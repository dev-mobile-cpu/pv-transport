package com.pv.transport.data.log

abstract class BaseResponse<T> {
    abstract val isSuccess: Boolean
    abstract val message: String
    abstract val data: T?
}