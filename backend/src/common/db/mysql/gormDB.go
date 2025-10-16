package mysql

import (
	"gorm.io/gorm"
)

type ExperimentSessions struct {
	gorm.Model
	VarThreshold  float64 `json:"var_threshold" gorm:"column:var_threshold"`
	LearningRate  float64 `json:"learning_rate" gorm:"column:learning_rate"`
	Iterations    int     `json:"iterations" gorm:"column:iterations"`
	LearningPath  string  `json:"learning_path" gorm:"column:learning_path"`
	TestImagePath string  `json:"test_image_path" gorm:"column:test_image_path"`
	RoiPath       string  `json:"roi_path" gorm:"column:roi_path"`
	Name          string  `json:"name" gorm:"column:name"`
	ProjectId     string  `json:"project_id" gorm:"column:project_id"`
}

type CctvResults struct {
	gorm.Model
	ExperimentSessionId int    `json:"experiment_session_id" gorm:"column:experiment_session_id"`
	CctvId              string `json:"cctv_id" gorm:"column:cctv_id"`
	LearningDataSize    int    `json:"learning_data_size" gorm:"column:learning_data_size"`
}

type RoiResults struct {
	gorm.Model
	CctvResultId int     `json:"cctv_result_id" gorm:"column:cctv_result_id"`
	RoiId        int     `json:"roi_id" gorm:"column:roi_id"`
	Rate         float64 `json:"rate" gorm:"column:rate"`
}
