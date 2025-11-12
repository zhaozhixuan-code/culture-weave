// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /post/add */
export async function addPost(
  body: {
    postAddRequest: API.PostAddRequest
  },
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponsePostVO>('/post/add', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /post/delete */
export async function deletePost(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/post/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /post/get/vo */
export async function getResourcesVoById1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getResourcesVOById1Params,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePostVO>('/post/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /post/list/page */
export async function listPostByPage(body: API.PostQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePagePost>('/post/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /post/list/page/vo */
export async function listResourcesVoByPage1(
  body: API.PostQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePostVO>('/post/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /post/update */
export async function updateResource1(
  body: {
    postUpdateRequest: API.PostUpdateRequest
  },
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/post/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
