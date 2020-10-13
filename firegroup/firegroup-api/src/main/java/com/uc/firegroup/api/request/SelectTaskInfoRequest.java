package com.uc.firegroup.api.request;

import java.io.Serializable;
import java.util.List;
import com.uc.firegroup.api.response.GroupAllByMerchantRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("剧本任务查询实体")
public class SelectTaskInfoRequest implements Serializable {
	private static final long serialVersionUID = 8480132207505926415L;

	@ApiModelProperty("任务名称")
	private String taskName;
	@ApiModelProperty("任务id")
	private Integer taskId;
	@ApiModelProperty("任务下的群实体集合")
	private List<GroupAllByMerchantRequest> groups;
	@ApiModelProperty("任务下所有群水军总数")
	private int totalRobot;
}
