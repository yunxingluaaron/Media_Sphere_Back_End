<html>
<head>
    <title>Hello Freemarker</title>
</head>
<body>

<div>hello ${there}</div>
<br>
<div>
    用户id：${stu.uid}<br/>
    用户姓名：${stu.username}<br/>
    年龄：${stu.age}<br/>
    生日：${stu.birthday?string('yyyy-MM-dd HH:mm:ss')}<br/>
    余额：${stu.amount}<br/>
    已育：${stu.haveChild?string('yes', 'no')}<br/>
    伴侣：${stu.spouse.username}, ${stu.spouse.age}岁

    <br>
    <div>
        <#list stu.articleList as article>
            <div>
                <span>${article.id}</span>
                <span>${article.title}</span>
            </div>
        </#list>
    </div>

    <br>
    <div>
        <#list stu.parents?keys as key>
            <div>
                ${stu.parents[key]}

            </div>
        </#list>
    </div>

    <br>
            <div>
                <#if stu.uid == '10010'>
                    User id is 10010
                </#if>
                <br>
                <#if stu.username != 'inews'>
                    User id is not 10010
                </#if>
                <br>
                <#if (stu.age >= 18)>
                    You're are a adult
                </#if>
                <br>
                <#if (stu.age < 18)>
                    You're are under 18
                </#if>
                <br>
                <#if stu.haveChild>
                    Have child!
                </#if>
                <br>
                <#if !stu.haveChild>
                    No Child!
                </#if>
                <br>
                <#if stu.spouse??>
                    Married
                </#if>
                <br>
                <#if !stu.spouse??>
                    Single
                </#if>
            </div>
</div>
</body>
</html>