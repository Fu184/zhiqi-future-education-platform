// BCrypt 上限按 UTF-8 字节计算，与服务端使用同一规则。
export function assertPassword(value) {
  if (typeof value !== 'string' || value.length < 8 || new TextEncoder().encode(value).length > 72)
    throw new Error('密码至少 8 个字符，UTF-8 编码不超过 72 字节')
}
