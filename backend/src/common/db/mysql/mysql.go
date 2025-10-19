package mysql

import (
	"database/sql"
	"fmt"
	"os"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"github.com/google/uuid"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

var MysqlDB *sql.DB
var GormMysqlDB *gorm.DB

const DBTimeOut = 8 * time.Second

func InitMySQL() error {
	var connectionString string
	var err error
	isLocal := os.Getenv("IS_LOCAL")
	if isLocal == "true" {
		// MySQL 연결 문자열
		connectionString = fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?parseTime=true",
			os.Getenv("MYSQL_USER"),
			os.Getenv("MYSQL_PASSWORD"),
			os.Getenv("MYSQL_HOST"),
			os.Getenv("MYSQL_PORT"),
			os.Getenv("MYSQL_DATABASE"),
		)
	}
	fmt.Println("Connection string from env:", connectionString)

	// 하드코딩된 연결 문자열 (환경 변수가 없을 때)
	if connectionString == "" {
		connectionString = "root:examplepassword@tcp(13.203.37.93:3306)/daily_dev?parseTime=true"
		fmt.Println("Using hardcoded connection string:", connectionString)
	}

	// MySQL에 연결 - 전역 변수에 할당 (중요: := 가 아니라 = 사용)
	var mysqlErr error
	MysqlDB, mysqlErr = sql.Open("mysql", connectionString)
	if mysqlErr != nil {
		fmt.Println("Failed to connect to MySQL!")
		fmt.Printf("에러 메시지: %s\n", mysqlErr)
		return mysqlErr
	}

	// 연결 테스트
	if pingErr := MysqlDB.Ping(); pingErr != nil {
		fmt.Println("Failed to ping MySQL!")
		fmt.Printf("에러 메시지: %s\n", pingErr)
		return pingErr
	}
	fmt.Println("Connected to MySQL!")

	// GORM 초기화 - 전역 변수에 할당
	GormMysqlDB, err = gorm.Open(mysql.New(mysql.Config{
		Conn: MysqlDB,
	}), &gorm.Config{
		SkipDefaultTransaction: false,
	})
	if err != nil {
		fmt.Println("Failed to connect to Gorm MySQL!")
		fmt.Printf("에러 메시지: %s\n", err)
		return err
	}

	fmt.Println("GORM MySQL initialized successfully!")

	return nil
}

func PKIDGenerate() string {
	//uuid 로 생성
	result := (uuid.New()).String()
	return result
}

func NowDateGenerate() string {
	return time.Now().Format("2006-01-02 15:04:05")
}

func EpochToTime(t int64) time.Time {
	return time.Unix(t, t%1000*1000000)
}
func EpochToTimeString(t int64) string {
	return time.Unix(t, t%1000*1000000).String()
}

func TimeStringToEpoch(t string) int64 {
	date, _ := time.Parse("2006-01-02 15:04:05 -0700 MST", t)
	return date.Unix()
}

func TimeToEpoch(t time.Time) int64 {
	return t.Unix()
}

// 트랜잭션 처리 미들웨어
func Transaction(db *gorm.DB, fc func(tx *gorm.DB) error) (err error) {
	tx := db.Begin()
	defer func() {
		if r := recover(); r != nil {
			tx.Rollback()
			err = fmt.Errorf("panic occurred: %v", r)
		} else if err != nil {
			tx.Rollback()
		} else {
			err = tx.Commit().Error
		}
	}()

	if err = tx.Error; err != nil {
		return err
	}

	err = fc(tx)
	return
}
