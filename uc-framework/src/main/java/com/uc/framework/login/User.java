package com.uc.framework.login;

import java.io.Serializable;
import java.util.List;

/***
 * 
 * title: 各种项目 user汇总
 *
 * @author HadLuo
 * @date 2020-10-9 11:13:29
 */
public class User implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4960207461609891891L;
    /** 账户级别 */
    private String acctLevel;
    /** 账户级别 */
    private String acctName;
    /** 用户附加属性 */
    private Object attrs;
    /** 是否有效:0无效1有效 */
    private Integer isEnabled;
    /** 菜单模板集合 */
    private List<String> menuTemplateIds;
    /** 商家类型 0:比邻1:标品2:商家 */
    private Integer merchantType;
    /** 商家编码 */
    private String merchatId;
    /** 父级ID */
    private String parentId;
    /** 登陆密码 */
    private String password;
    /** 手机号 */
    private String phoneNum;
    /** 状态 */
    private Integer status;
    /** 用户ID */
    private String sysUserId;
    /** 登陆账号 */
    private String username;
    /** 登录类型 */
    private UserType userType;

    // ********************留客字段********************************
    // accountType：账号类型 0：商家 1：操作员 2：客服
    private Integer accountType;
    // userCode：登录账号
    private String userCode;

    public enum UserType {
        /** 比邻 用户信息 */
        Bearer,
        /** 留客用户信息 */
        liuKe
    }

    public void registerType(UserType userType) {
        this.userType = userType;
    }

    public UserType userType() {
        return userType;
    }

    public String getAcctName() {
        return acctName;
    }

    public void setAcctName(String acctName) {
        this.acctName = acctName;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Integer getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(Integer merchantType) {
        this.merchantType = merchantType;
    }

    public String getMerchatId() {
        return merchatId;
    }

    public void setMerchatId(String merchatId) {
        this.merchatId = merchatId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAcctLevel() {
        return acctLevel;
    }

    public void setAcctLevel(String acctLevel) {
        this.acctLevel = acctLevel;
    }

    public Object getAttrs() {
        return attrs;
    }

    public void setAttrs(Object attrs) {
        this.attrs = attrs;
    }

    public List<String> getMenuTemplateIds() {
        return menuTemplateIds;
    }

    public void setMenuTemplateIds(List<String> menuTemplateIds) {
        this.menuTemplateIds = menuTemplateIds;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSysUserId() {
        return sysUserId;
    }

    public void setSysUserId(String sysUserId) {
        this.sysUserId = sysUserId;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserCode() {
        return userCode;
    }

    @Override
    public String toString() {
        return "User [acctLevel=" + acctLevel + ", acctName=" + acctName + ", attrs=" + attrs + ", isEnabled="
                + isEnabled + ", menuTemplateIds=" + menuTemplateIds + ", merchantType=" + merchantType
                + ", merchatId=" + merchatId + ", parentId=" + parentId + ", password=" + password
                + ", phoneNum=" + phoneNum + ", status=" + status + ", sysUserId=" + sysUserId + ", username="
                + username + ", userType=" + userType + ", accountType=" + accountType + ", userCode="
                + userCode + "]";
    }

}
