package request

type ReqSignUp struct {
	AccountID string `json:"account_id" binding:"required"`
	Password  string `json:"password" binding:"required"`
	AuthCode  string `json:"auth_code" binding:"required"`
	NickName  string `json:"nickname" binding:"required"`
}
