package com.uc.firegroup.api.request;

import java.io.Serializable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Api(tags = "内部工具，不提供出去")
public class InstrumentOpRequest implements Serializable {
    private static final long serialVersionUID = -4655630842132767753L;

    @Data
    @Api(tags = "redis操作")
    public static class RedisOp implements Serializable {
        private static final long serialVersionUID = 4194433690207524294L;
        @ApiModelProperty("redis方法:    get ,set,del,lrange,lpush,lpush")
        private String op;
        @ApiModelProperty("redis key")
        private String key;
        @ApiModelProperty("redis 值")
        private String value;
    }

    @Data
    @Api(tags = "mysql操作")
    public static class MysqlOp implements Serializable {
        private static final long serialVersionUID = 3496201894332995290L;
        @ApiModelProperty("要执行的sql")
        private String sql;
    }

}
