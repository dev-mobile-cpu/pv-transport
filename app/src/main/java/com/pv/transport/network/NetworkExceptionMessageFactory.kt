package com.pv.transport.network

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface NetworkExceptionMessageFactory {

    fun getErrorMessage(networkException: NetworkException): CharSequence

    fun getErrorMessage(unknownHostException: UnknownHostException): CharSequence

    fun getErrorMessage(socketTimeoutException: SocketTimeoutException): CharSequence

    fun getErrorMessage(connectException: ConnectException): CharSequence

    fun getErrorMessage (ioException : IOException) : CharSequence
}