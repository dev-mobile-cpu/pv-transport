package com.pv.transport.network

import okio.IOException

class NoInternetException(message: String) : IOException(message)