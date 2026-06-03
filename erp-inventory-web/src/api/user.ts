import request from './request'

export interface UserInfoResponse {
  id?: number
  username?: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  token?: string
  roleId?: number
  roleCode?: string
  roleName?: string
  permissions?: string[]
}

export interface UserListItem {
  id?: number
  username?: string
  realName?: string
  email?: string
  phone?: string
  status?: number
  roleId?: number
  roleCode?: string
  roleName?: string
}

export const userApi = {
  login(data: any) {
    return request.post('/user/login', data)
  },

  register(data: any) {
    return request.post('/user/register', data)
  },

  getUserInfo() {
    return request.get('/user/info')
  },

  list() {
    return request.get('/user/list')
  },

  update(data: Partial<UserListItem>) {
    return request.put('/user', data)
  },

  delete(id: number) {
    return request.delete(`/user/${id}`)
  }
}
