package domain

import "errors"

var (
	// ErrInternal indicates an unexpected server-side error.
	ErrInternal = errors.New("internal error")
)
