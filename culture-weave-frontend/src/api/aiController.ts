// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /ai/chat/sse */
export async function doChatWithSse(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.doChatWithSSEParams,
  options?: { [key: string]: any }
) {
  return request<string[]>('/ai/chat/sse', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /ai/explain/resources */
export async function explainResourcesByChat(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.explainResourcesByChatParams,
  options?: { [key: string]: any }
) {
  return request<string[]>('/ai/explain/resources', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
