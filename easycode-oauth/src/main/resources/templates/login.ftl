<!DOCTYPE HTML>
<html>
<head>
  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
  <meta name="renderer" content="webkit">
  <meta http-equiv="Pragma" content="no-cache">
  <meta http-equiv="Cache-Control" content="no-cache">
  <meta http-equiv="Expires" content="0">
  <title>登录</title>
  <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M" crossorigin="anonymous">
  <link href="https://getbootstrap.com/docs/4.0/examples/signin/signin.css" rel="stylesheet" crossorigin="anonymous"/>
</head>
<body>
<div class="container">
  <form class="form-signin" method="post" action="/login">
    <h2 class="form-signin-heading">OAuth</h2>

    [#if errorMsg?? ]
    [#-- 显示错误信息 --]
      <div class="alert alert-danger" role="alert">${errorMsg?html}</div>
    [/#if]

    <p>
      <label for="username" class="sr-only">用户名</label>
      <input type="text" id="username" name="username" class="form-control" placeholder="Username" required autofocus>
    </p>
    <p>
      <label for="password" class="sr-only">密码</label>
      <input type="password" id="password" name="password" class="form-control" placeholder="Password" required>
    </p>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <button class="btn btn-lg btn-primary btn-block" type="submit">登录</button>
  </form>
</div>
</body>
</html>