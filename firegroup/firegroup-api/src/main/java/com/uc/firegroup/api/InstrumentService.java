package com.uc.firegroup.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.uc.firegroup.api.request.InstrumentOpRequest;

/***
 * 
 * title: 内联工具,只支持post请求
 *
 * @author HadLuo
 * @date 2020-9-22 13:54:54
 */
@FeignClient(name = Constant.instanceName)
public interface InstrumentService {
    /**
     * 
     * title: 执行redis操作
     *
     * @param op
     * @author HadLuo 2020-9-28 8:59:55
     */
    @PostMapping("/its/r")
    public String invokeRedis(@RequestBody InstrumentOpRequest.RedisOp op);

    /**
     * 
     * title:批量 执行redis操作
     *
     * @param op
     * @author HadLuo 2020-9-28 8:59:55
     */
    @PostMapping("/its/rs")
    public List<String> batchInvokeRedis(@RequestBody List<InstrumentOpRequest.RedisOp> ops);

    /**
     * 
     * title: 数据库操作
     *
     * @param op
     * @author HadLuo 2020-9-28 8:59:55
     */
    @PostMapping("/its/sql")
    public String sql(@RequestBody InstrumentOpRequest.MysqlOp op);

    /**
     * 
     * title: 批量数据库操作
     *
     * @param op
     * @author HadLuo 2020-9-28 8:59:55
     */
    @PostMapping("/its/sqls")
    public List<String> sqls(@RequestBody List<InstrumentOpRequest.MysqlOp> op);
}
