package response

type ResResetPassword struct {
	Success     bool   `json:"success"`
	NewPassword string `json:"new_password"`
	Message     string `json:"message"`
}
