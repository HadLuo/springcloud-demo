package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.GroupOnCallbackDTO;
import com.uc.external.bilin.req.PersonalWxInfoDTO;
import com.uc.external.bilin.req.SendPrivateChatMsgDTO;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.external.bilin.res.PersonalInfoVO;
import com.uc.external.bilin.res.ResultBody;
import com.uc.firegroup.api.IBilinService;
import com.uc.firegroup.api.pojo.RobotInfo;
import com.uc.firegroup.api.request.SendWxMsgRequest;
import com.uc.firegroup.service.mapper.RobotInfoMapper;
import com.uc.framework.Pair;
import com.uc.framework.logger.Logs;

import com.uc.framework.obj.Result;
import com.uc.framework.web.Rpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class IBilinServiceImpl implements IBilinService {
    @Value(value = "${identity}")
    private String identity;
    @Autowired
    private RobotInfoMapper robotInfoMapper;

    @Override
    public Result<String> getWxQRCode(String wxId) {
        PersonalWxInfoDTO personalWxInfoDTO = new PersonalWxInfoDTO();
        personalWxInfoDTO.setIdentity(identity);
        personalWxInfoDTO.setWxIds(Lists.newArrayList(wxId));
        Result<List<PersonalInfoVO>> r = Urls.queryPersonalInfo(personalWxInfoDTO);
        Logs.e(getClass(), "获取比邻返回的二维码信息" + JSON.toJSONString(r));
        if (r.getCode() != 0) {
            Logs.e(getClass(), "参数异常！");
            return Result.err(-1, "参数异常！");
        }
        if (r.getData() == null) {
            Logs.e(getClass(), "参数异常！");
            return Result.err(-1, "参数异常！");
        }
        String wxQRCode = r.getData().get(0).getWxRqUrl();
        GroupOnCallbackDTO dto = new GroupOnCallbackDTO();
        dto.setIdentity(identity);
        dto.setItemIds(Lists.newArrayList(wxId));
        ResultBody resultBody = Rpc.post("http://neighbour-message/partner/callback/personal/on", dto,
                ResultBody.class);
        System.err.println(JSON.toJSON(resultBody));
        return Result.ok(wxQRCode);
    }

    @Override
    public Result<Void> sendWxMsg(SendWxMsgRequest request) {
        // 接受消息方是用户才发
        RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(request.getToWxId());
        if (robotInfo != null) {
            return Result.err(-1, "接受消息人为机器人" + JSON.toJSONString(robotInfo));
        }
        SendPrivateChatMsgDTO dto = new SendPrivateChatMsgDTO();
        dto.setWxId(request.getFromWxId());
        dto.setIdentity(identity);
        dto.setMerchatId(request.getMerchantId());
        dto.setFreWxId(request.getToWxId());
        SendPrivateChatMsgDTO.Data data = new SendPrivateChatMsgDTO.Data();
        data.setMsgContent(request.getMsgContent());
        data.setMsgType(2001);
        dto.setData(Lists.newArrayList(data));
        Result<BooleanResultVo> r = Urls.sendPrivateChatMsg(dto);
        return Result.ok(null);
    }

    public Pair<Boolean, Object> robotHasEnable(String wxId) {
        if (StringUtils.isEmpty(wxId)) {
            return Pair.of(false, null);
        }
        PersonalWxInfoDTO dto = new PersonalWxInfoDTO();
        dto.setIdentity(identity);
        dto.setWxIds(Lists.newArrayList(wxId));
        Result<List<PersonalInfoVO>> result = Urls.queryPersonalInfo(dto);
        if (result.successful() && !CollectionUtils.isEmpty(result.getData())) {
            // 是否离线
            PersonalInfoVO infoVO = result.getData().get(0);
            if ("0".equals(infoVO.getIsClose()) && "1".equals(infoVO.getIsOnline())) {
                // 机器人 正常
                return Pair.of(true, infoVO);
            }
        }
        return Pair.of(false, null);
    }
}
