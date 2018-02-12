package com.abhishek.common

case class RegisterRequest (msisdn :Option[String] = None)

case class LoginRequest (msisdn :Option[String] = None, uid :Option[String] = None)
