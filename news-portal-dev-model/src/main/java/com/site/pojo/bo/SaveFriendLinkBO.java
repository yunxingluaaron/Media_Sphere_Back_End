package com.site.pojo.bo;

import com.site.validate.CheckUrl;
import com.site.validate.CheckUrlName;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SaveFriendLinkBO {
    private String id;
    @NotBlank(message = "Link name cannot be empty")
    @CheckUrlName
    private String linkName;
    @NotBlank(message = "Link address cannot be empty")
    @CheckUrl
    private String linkUrl;
    @NotNull(message = "Please select delete or modify")
    private Integer isDelete;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

}