package domain

import "fmt"

// ErrorType represents the category of the error.
type ErrorType string

const (
	TypeNotFound           ErrorType = "NOT_FOUND"
	TypeAlreadyExists      ErrorType = "ALREADY_EXISTS"
	TypeInvalidCredentials ErrorType = "INVALID_CREDENTIALS"
	TypeUnauthorized       ErrorType = "UNAUTHORIZED"
	TypeInternal           ErrorType = "INTERNAL"
	TypeUnprocessable      ErrorType = "UNPROCESSABLE"
)

// AppError is a custom error type that carries a category and a specific message.
type AppError struct {
	Type    ErrorType
	Message string
}

func (e AppError) Error() string {
	return e.Message
}

// Helper functions to create specific AppErrors with custom messages.

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

func NewUnprocessableEntityError(message string) error {
	return AppError{Type: TypeUnprocessable, Message: message}
}

func NewInternalError(message string) error {
	return AppError{Type: TypeInternal, Message: message}
}

// NewResourceNotFound is a specific helper like the Java counterpart.
func NewResourceNotFound(resource, field string, value any) error {
	return NewNotFoundError(fmt.Sprintf("%s not found with %s: '%v'", resource, field, value))
}
