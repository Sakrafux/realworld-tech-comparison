package errors

import "fmt"

type ErrorType string

const (
	TypeNotFound           ErrorType = "NOT_FOUND"
	TypeAlreadyExists      ErrorType = "ALREADY_EXISTS"
	TypeInvalidCredentials ErrorType = "INVALID_CREDENTIALS"
	TypeUnauthorized       ErrorType = "UNAUTHORIZED"
	TypeForbidden          ErrorType = "FORBIDDEN"
	TypeInternal           ErrorType = "INTERNAL"
	TypeUnprocessable      ErrorType = "UNPROCESSABLE"
)

type AppError struct {
	Type    ErrorType
	Message string
}

func (e AppError) Error() string {
	return e.Message
}

func NewNotFoundError(message string) error {
	return AppError{Type: TypeNotFound, Message: message}
}

func NewAlreadyExistsError(message string) error {
	return AppError{Type: TypeAlreadyExists, Message: message}
}

func NewInvalidCredentialsError(message string) error {
	return AppError{Type: TypeInvalidCredentials, Message: message}
}

func NewUnauthorizedError(message string) error {
	return AppError{Type: TypeUnauthorized, Message: message}
}

func NewForbiddenError(message string) error {
	return AppError{Type: TypeForbidden, Message: message}
}

func NewUnprocessableEntityError(message string) error {
	return AppError{Type: TypeUnprocessable, Message: message}
}

func NewInternalError(message string) error {
	return AppError{Type: TypeInternal, Message: message}
}

func NewResourceNotFound(resource, field string, value any) error {
	return NewNotFoundError(fmt.Sprintf("%s not found with %s: '%v'", resource, field, value))
}
