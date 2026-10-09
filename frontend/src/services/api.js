export async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    credentials: 'same-origin',
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  })
  const data = await response.json().catch(() => ({ message: '服务返回了无效的响应' }))
  if (!response.ok) {
    const error = new Error(data.message || '请求失败')
    error.status = response.status
    throw error
  }
  return data
}
