package com.uc.firegroup.service.impl;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.firegroup.api.InstrumentService;
import com.uc.firegroup.api.request.InstrumentOpRequest.MysqlOp;
import com.uc.firegroup.api.request.InstrumentOpRequest.RedisOp;
import com.uc.framework.db.JDBC;
import com.uc.framework.logger.Logs;
import com.uc.framework.redis.RedisHandler;

/***
 * 
 * title: 内联工具,只支持get请求
 *
 * @author HadLuo
 * @date 2020-9-22 13:54:54
 */
@RestController
public class InstrumentServiceImpl implements InstrumentService {
    @Autowired
    private JDBC jdbc;

    @Override
    public String invokeRedis(RedisOp op) {
        if ("get".equalsIgnoreCase(op.getOp().trim())) {
            return RedisHandler.get(op.getKey());
        }
        if ("set".equalsIgnoreCase(op.getOp().trim())) {
            RedisHandler.set(op.getKey(), op.getValue());
            return "ok";
        }
        if ("del".equalsIgnoreCase(op.getOp().trim())) {
            RedisHandler.del(op.getKey());
            return "ok";
        }
        if ("lrange".equalsIgnoreCase(op.getOp().trim())) {
            return JSON.toJSONString(RedisHandler.lrange(op.getKey(), 0, -1));
        }
        if ("lpush".equalsIgnoreCase(op.getOp().trim())) {
            RedisHandler.lpush(op.getKey(), op.getValue());
            return "ok";
        }
        if ("lpush".equalsIgnoreCase(op.getOp().trim())) {
            RedisHandler.rpush(op.getKey(), op.getValue());
            return "ok";
        }
        return "不支持的redis 操作";
    }

    @Override
    public List<String> batchInvokeRedis(List<RedisOp> ops) {
        List<String> res = Lists.newArrayList();
        for (RedisOp op : ops) {
            try {
                res.add(invokeRedis(op));
            } catch (Exception e) {
                Logs.e(getClass(), "invoke工具失败", e);
            }
        }
        return res;
    }

    @Override
    public String sql(MysqlOp op) {
        try {
            return jdbc.exec(op.getSql()) + "";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public List<String> sqls(List<MysqlOp> ops) {
        List<String> res = Lists.newArrayList();
        for (MysqlOp op : ops) {
            try {
                res.add(sql(op));
            } catch (Exception e) {
                Logs.e(getClass(), "invoke工具失败", e);
            }
        }
        return res;
    }

}
