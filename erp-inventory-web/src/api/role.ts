import request from './request'

export interface RoleItem {
  id?: number
  roleName?: string
  roleCode?: string
  status?: number
  remark?: string
}

export const roleApi = {
  list() {
    return request.get('/role/list')
  }
}
