declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseCommentVO = {
    code?: number
    data?: CommentVO
    message?: string
  }

  type BaseResponseListString = {
    code?: number
    data?: string[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageCommentVO = {
    code?: number
    data?: PageCommentVO
    message?: string
  }

  type BaseResponsePagePost = {
    code?: number
    data?: PagePost
    message?: string
  }

  type BaseResponsePagePostVO = {
    code?: number
    data?: PagePostVO
    message?: string
  }

  type BaseResponsePageResources = {
    code?: number
    data?: PageResources
    message?: string
  }

  type BaseResponsePageResourcesVO = {
    code?: number
    data?: PageResourcesVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponsePostVO = {
    code?: number
    data?: PostVO
    message?: string
  }

  type BaseResponseResourcesCategory = {
    code?: number
    data?: ResourcesCategory
    message?: string
  }

  type BaseResponseResourcesVO = {
    code?: number
    data?: ResourcesVO
    message?: string
  }

  type BaseResponseSetString = {
    code?: number
    data?: string[]
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type CommentAddRequest = {
    postId?: number
    content?: string
  }

  type CommentQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    postId?: number
  }

  type CommentVO = {
    id?: number
    postId?: number
    content?: string
    createTime?: string
    userId?: number
    creator?: UserVO
  }

  type DeleteRequest = {
    id?: number
  }

  type doChatWithSSEParams = {
    message: string
    chatId: string
  }

  type explainResourcesByChatParams = {
    resourcesId: number
  }

  type getResourcesVOById1Params = {
    id: number
  }

  type getResourcesVOByIdParams = {
    id: number
  }

  type getSearchHistoryParams = {
    UserId: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type PageCommentVO = {
    records?: CommentVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageCommentVO
    searchCount?: PageCommentVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PagePost = {
    records?: Post[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePost
    searchCount?: PagePost
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PagePostVO = {
    records?: PostVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePostVO
    searchCount?: PagePostVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageResources = {
    records?: Resources[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageResources
    searchCount?: PageResources
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageResourcesVO = {
    records?: ResourcesVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageResourcesVO
    searchCount?: PageResourcesVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageUserVO = {
    records?: UserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUserVO
    searchCount?: PageUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type Post = {
    id?: number
    pictureUrl?: string
    title?: string
    content?: string
    userId?: number
    authorization?: number
    statement?: number
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type PostAddRequest = {
    id?: number
    title?: string
    content?: string
    authorization?: number
    statement?: number
  }

  type PostQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    searchText?: string
    pictureUrl?: string
    title?: string
    content?: string
  }

  type PostUpdateRequest = {
    id?: number
    title?: string
    content?: string
    imageUrl?: string
    authorization?: number
    statement?: number
  }

  type PostVO = {
    id?: number
    pictureUrl?: string
    title?: string
    content?: string
    userId?: number
    authorization?: number
    statement?: number
    createTime?: string
    userVO?: UserVO
  }

  type Resources = {
    id?: number
    name?: string
    introduction?: string
    category?: string
    tags?: string
    region?: string
    resourceImgUrl?: string
    price?: number
    userId?: number
    userName?: string
    userDescription?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ResourcesAddRequest = {
    id?: number
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    region?: string
    userName?: string
    userDescription?: string
    price?: number
  }

  type ResourcesCategory = {
    categoryList?: string[]
    regionList?: string[]
  }

  type ResourcesQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    searchText?: string
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    region?: string
    userName?: string
  }

  type ResourcesUpdateRequest = {
    id?: number
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    region?: string
    userName?: string
    userDescription?: string
    price?: number
  }

  type ResourcesVO = {
    id?: number
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    region?: string
    resourceImgUrl?: string
    price?: number
    userId?: number
    userVO?: UserVO
    userName?: string
    userDescription?: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserEditRequest = {
    id?: number
    userName?: string
    userProfile?: string
    userPassword?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
