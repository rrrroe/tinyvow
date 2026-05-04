# 国内版激活码说明

国内版使用本地身份和签名激活码，不依赖后端。

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

## 限制

- 激活码绑定国内版本地用户 ID。
- 用户卸载、清除数据或换设备后，本地身份和激活状态可能丢失。
- 无后端时只能用本机系统时间判断到期；App 会检测明显时间回拨，但不能替代服务器时间。
