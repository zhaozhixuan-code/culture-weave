// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /resource/add */
export async function addResource(
  body: {
    resourcesAddRequest: API.ResourcesAddRequest
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

  return request<API.BaseResponseResourcesVO>('/resource/add', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /resource/category_list */
export async function getResourceCategoryList(options?: { [key: string]: any }) {
  return request<API.BaseResponseResourcesCategory>('/resource/category_list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /resource/delete */
export async function deleteResource(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/resource/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /resource/get/vo */
export async function getResourcesVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getResourcesVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseResourcesVO>('/resource/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /resource/list/page */
export async function listResourcesByPage(
  body: API.ResourcesQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageResources>('/resource/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /resource/list/page/vo */
export async function listResourcesVoByPage(
  body: API.ResourcesQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageResourcesVO>('/resource/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /resource/update */
export async function updateResource(
  body: API.ResourcesUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/resource/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
