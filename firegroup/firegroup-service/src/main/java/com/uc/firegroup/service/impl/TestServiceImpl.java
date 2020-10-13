package com.uc.firegroup.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.spring.support.DelayedQueueDecorator;
//import com.uc.test.service.config.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.GroupOnCallbackDTO;
import com.uc.external.bilin.req.InputDTO;
import com.uc.external.bilin.req.MerchantBaseGpDTO;
import com.uc.external.bilin.req.PersonalWxInfoDTO;
import com.uc.external.bilin.req.SendGroupChatMsgDTO;
import com.uc.external.bilin.req.SendPrivateChatMsgDTO;
import com.uc.external.bilin.req.SendPrivateChatMsgDTO.Data;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.external.bilin.res.IsGroupMembersVo;
import com.uc.external.bilin.res.MqCallbackMessage;
import com.uc.external.bilin.res.PersonalInfoVO;
import com.uc.external.bilin.res.ResultBody;
import com.uc.firegroup.api.IJobService;
import com.uc.firegroup.api.IPlayInfoService;
import com.uc.firegroup.api.ITestService;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.api.request.PlayInfoRequest;
import com.uc.firegroup.service.inner.chat.JobChatPushProcessor;
import com.uc.firegroup.service.mapper.MerchantApiMapper;
import com.uc.firegroup.service.tools.msg.MessageProcessor;
import com.uc.framework.App;
import com.uc.framework.Ids;
import com.uc.framework.Times;
import com.uc.framework.chat.AbstractChatLifeCycle;
import com.uc.framework.chat.Chat;
import com.uc.framework.chat.ChatConfigure;
import com.uc.framework.chat.ChatConfigureBuilder;
import com.uc.framework.chat.ChatGroup;
import com.uc.framework.chat.ChatGroupFactory;
import com.uc.framework.chat.DefaultChatRequest;
import com.uc.framework.chat.Future;
import com.uc.framework.chat.context.ChatProcessor;
import com.uc.framework.chat.context.ChatProcessorContext;
import com.uc.framework.chat.strategy.AckStrategy;
import com.uc.framework.chat.strategy.SendChatStrategy;
import com.uc.framework.logger.Logs;
import com.uc.framework.login.User;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.obj.Result;
import com.uc.framework.redis.RedisHandler;
import com.uc.framework.redis.queue.MessageListener;
import com.uc.framework.redis.queue.DelayQueue;
import com.uc.framework.redis.queue.Task;
import com.uc.framework.resources.FileLoads;
import com.uc.framework.web.Rpc;

@RestController
public class TestServiceImpl implements ITestService {
    @Value(value = "${identity}")
    private String identity;
    @Autowired
    private MerchantApiMapper merchantApiMapper;
    // @Autowired
    // private KafkaProducer kafkaProducer;

    private String merchatId = "1281068687005413376";

    @Override
    public Result<?> test(String msg) {
        Result<User> users = Urls.userInfoByToken("2600a850-6a99-49c8-8543-a48110ec32de");

        Logs.e(getClass(), "当前用户:" + UserThreadLocal.get(), new NullPointerException("XXX为空"));

        System.err.println("当前用户:" + UserThreadLocal.get());
        System.err.println(RedisHandler.get("token_user:faa4e026-8eb7-4476-8cae-a1e4f9aea17d"));
        System.err.println("=======test=======" + JSON.toJSONString(merchantApiMapper.selectAll()));
        return Result.ok(JSON.toJSONString(merchantApiMapper.selectAll()));
    }

    @Override
    public void sendMsg() {
        PersonalWxInfoDTO personalWxInfoDTO = new PersonalWxInfoDTO();
        personalWxInfoDTO.setIdentity(identity);
        personalWxInfoDTO.setWxIds(Lists.newArrayList("4D79C13FD49A592F4E1E5D1E9DD1012D"));
        Result<List<PersonalInfoVO>> r = Urls.queryPersonalInfo(personalWxInfoDTO);
        System.err.println(JSON.toJSON(r));

        GroupOnCallbackDTO dto = new GroupOnCallbackDTO();
        dto.setIdentity(identity);
        dto.setItemIds(Lists.newArrayList("4D79C13FD49A592F4E1E5D1E9DD1012D"));
        ResultBody resultBody = Rpc.post("http://neighbour-message/partner/callback/personal/on", dto,
                ResultBody.class);
        System.err.println(JSON.toJSON(resultBody));
        // User user = UserThreadLocal.get();
        // SendPrivateChatMsgDTO dto = new SendPrivateChatMsgDTO();
        // dto.setWxId("9A85D272CD5BCAF8BAA0FE6B519F27B3");
        // dto.setIdentity(identity);
        // dto.setMerchatId(user.getMerchatId());
        // dto.setFreWxId("A5EB401BEC1B1DEC9B6F26E18C90DDB1");
        // // dto.setRelaSerialNo("A5EB401BEC1B1DEC9B6F26E18C90DDB1");
        // Data data = new Data();
        // data.setMsgContent("aaaaaaassssssssdddddddddd");
        // data.setMsgType(2001);
        // dto.setData(Lists.newArrayList(data));
        // // dto.setMerchatId(merchatId);
        // Result<BooleanResultVo> r = Urls.sendPrivateChatMsg(dto);
        System.err.println(JSON.toJSONString(r));
    }

    @Autowired
    RedissonClient redissonClient;

    @Override
    public void testRedis() {
        InputDTO in = new InputDTO();
        in.setIdentity(identity);
        InputDTO.Data p = new InputDTO.Data();
        p.setWxIds(Lists.newArrayList("07DE458DA41C3752AK0E5A8758D911EC"));
        p.setVcGroupIds(Lists.newArrayList("7306F139ACC799B120A7C57CFB74D267"));
        in.setParams(p);
        Result<List<IsGroupMembersVo>> rv = Urls.isGroupMembers(in);
        System.err.println(rv);

    }

    public static class A implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 6509910009300615723L;
        String identity;
        String vcGroupId;
        String wxId;

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getVcGroupId() {
            return vcGroupId;
        }

        public void setVcGroupId(String vcGroupId) {
            this.vcGroupId = vcGroupId;
        }

        public String getWxId() {
            return wxId;
        }

        public void setWxId(String wxId) {
            this.wxId = wxId;
        }

    }

    @Override
    public void testInGroup() {
        A a = new A();
        a.identity = identity;
        a.vcGroupId = "A8CD6C1AD1FD34F092F69BCF53D776DD";

        a.wxId = "00BEA953AAF388CC29C85AD465C7C046";
        // a.vcGroupIds =
        // 07DE458DA41C3752AD0E5A8758D911EC
        // a.setSearchType(11);
        System.err.println(JSON.toJSONString(a));
        String str = Rpc.post("http://neighbour-business-extend/fnGroupBusi/queryGroupMermber", a,
                String.class);
        System.err.println(str);
    }

    @Autowired
    IPlayInfoService playInfoService;

    @Override
    public void testInsertPlay() {
        String json = FileLoads.loadString("createPlay.json");
        PlayInfoRequest re = JSON.parseObject(json, PlayInfoRequest.class);
        Result<?> r = playInfoService.create(re);
        System.err.println(JSON.toJSONString(r));
    }

    @Autowired
    IJobService pushMessageService;

    @Override
    public void testPush() {
        pushMessageService.scanTimePush();
    }

    @Override
    public void kaiqun() {
        PersonalWxInfoDTO dssto = new PersonalWxInfoDTO();
        dssto.setWxIds(Lists.newArrayList("13C0BF6BF3105571B432F742DBFCC1F3"));
        dssto.setIdentity(identity);
        Result<List<PersonalInfoVO>> r = Urls.queryPersonalInfo(dssto);

        GroupOnCallbackDTO dt = new GroupOnCallbackDTO();
        dt.setIdentity(identity);
        dt.setItemIds(Lists.newArrayList("7306F139ACC799B120A7C57CFB74D267"));
        Result<Void> hh = Urls.groupOnCallback(dt);

        SendGroupChatMsgDTO dto1 = new SendGroupChatMsgDTO();
        dto1.setIdentity(identity);
        dto1.setWxId("07DE458DA41C3752AD0E5A8758D911EC");
        dto1.setVcGroupId("7306F139ACC799B120A7C57CFB74D267");
        dto1.setMerchatId(merchatId);
        SendGroupChatMsgDTO.Data d = new SendGroupChatMsgDTO.Data();
        d.setMsgContent("我是大傻逼哦");
        d.setMsgType(2001);
        dto1.setData(Lists.newArrayList(d));

        Result<BooleanResultVo> rs = Urls.sendGroupChatMessages(dto1);
        System.err.println(rs);
        MerchantBaseGpDTO dto = new MerchantBaseGpDTO();
        dto.setIdentity(identity);
        dto.setMerchatId(merchatId);
        dto.setVcGroupId("7306F139ACC799B120A7C57CFB74D267");
        dto.setWxId("07DE458DA41C3752AD0E5A8758D911EC");
    }

//    ChatProcessor processor1 = ChatProcessorContext.createProcessor(new DefaultChatRequest("firegroup-job"));
//    ChatProcessor processor2 = ChatProcessorContext
//            .createProcessor(new KeyWordsChatRequest("firegroup-keywords"));
    @Autowired
    JobChatPushProcessor s;
    @Override
    public void q() {
//        s.onResume();
        // processor1.setConfigure(ChatConfigureBuilder.build().setLifeCycle(new
        // AbstractChatLifeCycle() {
        // @Override
        // public void onStartup(String groupUuid, LinkedList<Chat> chats) {
        // System.err.println("onStartup>>groupUuid=" + groupUuid);
        //
        // }
        //
        // @Override
        // public void onSingleSendError(String ackKey, String groupUuid, Chat
        // chat, String errorMessage) {
        // System.err.println("onSingleSendError>>groupUuid=" + groupUuid +
        // ",errorMessage="
        // + errorMessage + ",chat=" + chat);
        // }
        //
        // @Override
        // public void onSingleHalfSendSuccess(String groupUuid, String ackKey,
        // Chat chat) {
        // System.err.println("半消息>>groupUuid=" + groupUuid + ",ackKey=" +
        // ackKey + ",chat="
        // + JSON.toJSONString(chat));
        // }
        //
        // @Override
        // public void onSingleAckSendSuccess(String groupUuid, String ackKey,
        // Chat chat) {
        // System.err.println("ack消息>>groupUuid=" + groupUuid + ",ackKey=" +
        // ackKey + ",chat="
        // + JSON.toJSONString(chat));
        // }
        //
        // @Override
        // public void onFinish(String groupUuid) {
        // // TODO Auto-generated method stub
        //
        // }
        //
        // @Override
        // public void onPause(String groupUuid, String groupWxId) {
        // // TODO Auto-generated method stub
        //
        // }
        //
        // @Override
        // public void onResume(String groupUuid, String groupWxId) {
        // // TODO Auto-generated method stub
        //
        // }
        // }).setSendChatStrategy(new SendChatStrategy() {
        // @Override
        // public Future asyncSend(Chat chat) {
        // String ackKey = Ids.getId();
        // // 比例发送消息
        // return Future.newSuccessFuture(ackKey, chat.getGroupUuid());
        // }
        // }));
        // List<Chat> chats = new ArrayList<Chat>();
        // // 12 个发言人
        // for (int i = 0; i < 3; i++) {
        // // 2个群
        // Chat chat = new Chat(10 + i, i, "group:11");
        // chat.setArg("我是第" + i + "条消息");
        // chats.add(chat);
        // }
        // ChatGroup chatGroup = ChatGroupFactory.create("123", chats);
        // processor1.onAccept(chatGroup);
    }

    @Override
    public void ack(String ids) {
        String[] id = ids.split(",");
//        processor1.onAck(() -> Future.newSuccessFuture(id[0], id[1]));
    }
}
