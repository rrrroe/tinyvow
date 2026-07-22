# 国内版激活码说明

国内版保留本地签名激活码，同时支持 Tiny Vow 服务端账号、服务端激活码和国内直营购买。旧 `TVA1` 本地激活码继续离线验证；`TVB1` 激活码、账号资料和直营订单通过 `https://api.tinyvow.rorolo.com` 处理。

## 生成密钥

```powershell
javac tools\activation\ActivationCodeTool.java
java -cp tools\activation ActivationCodeTool generate-keypair
```

- 私钥：`tools/activation/private_key.pkcs8`，已加入 `.gitignore`，不要提交。
- 公钥：`tools/activation/public_key.x509`。`china` flavor 构建时会优先读取这个文件，写入 `ACTIVATION_PUBLIC_KEY_BASE64`。
- 重新生成密钥后，必须重新构建并安装国内版 App；旧 App 内置的是旧公钥，无法验证新私钥签出的激活码。

## 给用户发激活码

让用户在国内版「我的 > 订阅 > 输入激活码」里复制用户 ID，然后执行：

```powershell
java -cp tools\activation ActivationCodeTool issue-code --user-id 用户ID --days 30
```

默认激活码 7 天内可兑换。可用 `--valid-days` 修改兑换有效期：

```powershell
java -cp tools\activation ActivationCodeTool issue-code --user-id 用户ID --days 90 --valid-days 14
```

## 验证激活码

```powershell
java -cp tools\activation ActivationCodeTool verify-code --code 激活码
```

如果脚本能验证通过，但实机提示激活码无效，优先检查：

- 用户 ID 是否完全一致。
- App 是否是重新构建安装后的 `chinaDebug` 或 `chinaRelease`。
- `tools/activation/public_key.x509` 是否和当前私钥配套。
- 激活码复制时是否多了空格、换行或漏掉字符。

## 账号与兼容性

- App 首次建立匿名设备身份；注册邮箱账号时会原地升级，保留已有服务端权益和订单。
- 在其他设备登录时，当前设备的匿名权益和订单会在设备上限允许时合并到正式账号。
- 用户注册或登录正式账号后，App 会自动尝试把本机仍有效的旧 `TVA1` 会员认领到服务器。服务器只接受已通过旧公钥验签并预登记的 `codeId`；认领成功后，用户换设备登录也能恢复该会员。
- 旧 `TVA1` 码需要先通过后端内部接口 `/internal/admin/activation-codes/legacy/import` 导入。导入过程验证原始签名，服务器不保存激活码明文，只保存签发元数据和本地用户 ID 的不可逆哈希。
- 如果旧码尚未导入或发生网络错误，App 继续使用本机会员状态并在后续刷新时重试，不会因为登录失败或迁移失败取消本地权益。
- 邮箱密码仅用于登录；服务端保存 BCrypt 哈希，客户端不保存明文密码。
- 约定、使用记录、积分、步数、专注和战报仍只保存在本机，不随账号上传。

## 本地激活限制

- 激活码绑定国内版本地用户 ID。
- 用户卸载、清除数据或换设备后，本地身份和激活状态可能丢失。
- 无后端时只能用本机系统时间判断到期；App 会检测明显时间回拨，但不能替代服务器时间。
