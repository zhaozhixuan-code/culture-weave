// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /resource/add */
export async function addResource(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.addResourceParams,
  body: {},
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseResourcesVO>('/resource/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
      resourcesAddRequest: undefined,
      ...params['resourcesAddRequest'],
    },
    data: body,
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
export async function getPictureVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVOByIdParams,
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
export async function listPictureByPage(
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
export async function listPictureVoByPage(
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
