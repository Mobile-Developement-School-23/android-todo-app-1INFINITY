package ru.mirea.ivashechkinav.todo.core

open class AppException : Exception()

class OutOfSyncDataException : AppException()
class TodoItemNotFoundException : AppException()
class BadRequestException : AppException()
class UnauthorizedException : AppException()
class ServerSideException : AppException()
class DuplicateItemException : AppException()
class NetworkException : AppException()
class UnableToPerformOperation: AppException()
