package com.site.enums;

/**
 * @Desc: 用户状态 枚举
 */
public enum UserStatus {
//    用户状态：
//    0：Unactivated。
//    1：Activated：基本信息是否完善，真实姓名，邮箱地址，性别，生日，住址等，
//              如果没有完善，则用户不能发表评论，不能点赞，不能关注，不能操作任何入库的功能。
//    2：Blocked。


    INACTIVE(0, "Inactivated"),
    ACTIVE(1, "Activated"),
    FROZEN(2, "Blocked");

    public final Integer type;
    public final String value;

    UserStatus(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * 判断传入的用户状态是不是有效的值
     * @param tempStatus
     * @return
     */
    public static boolean isUserStatusValid(Integer tempStatus) {
        if (tempStatus != null) {
            if (tempStatus == INACTIVE.type || tempStatus == ACTIVE.type || tempStatus == FROZEN.type) {
                return true;
            }
        }
        return false;
    }

}
