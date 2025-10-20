package common

import (
	"fmt"
	"main/common/db/mysql"
	"main/common/storage"
)

func InitServer() error {
	if err := InitEnv(); err != nil {
		fmt.Sprintf("서버 에러 발생 : %s", err.Error())
		return err
	}

	if err := InitJwt(); err != nil {
		fmt.Sprintf("jwt 초기화 에러 : %s", err.Error())
		return err
	}

	if err := mysql.InitMySQL(); err != nil {
		fmt.Sprintf("db 초기화 에러 : %s", err.Error())
		return err
	}

	// S3 초기화 (AWS 자격 증명이 없어도 에러로 처리하지 않음)
	s3Config := storage.S3Config{
		Region:          Env.AWSRegion,
		AccessKeyID:     Env.AWSAccessKeyID,
		SecretAccessKey: Env.AWSSecretAccessKey,
		BucketName:      Env.S3BucketName,
		Endpoint:        Env.S3Endpoint,
	}
	if err := storage.InitS3(s3Config); err != nil {
		fmt.Printf("⚠️  S3 초기화 경고: %s (S3 업로드 기능 비활성화)\n", err.Error())
	}

	if !Env.IsLocal {
		if err := InitLogging(); err != nil {
			return err
		}
	}
	return nil
}
