package utils

import (
	"encoding/base64"
	"golang.org/x/crypto/sha3"
	"math/rand"
	"time"
	"unsafe"
)

const letterBytes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
const (
	letterIdxBits = 6                    // 6 bits to represent a letter index
	letterIdxMask = 1<<letterIdxBits - 1 // All 1-bits, as many as letterIdxBits
	letterIdxMax  = 63 / letterIdxBits   // # of letter indices fitting in 63 bits
)

var src = rand.NewSource(time.Now().UnixNano())

func RandomLong() uint64 {
	return uint64(src.Int63())
}

func RandomInt() uint32 {
	return uint32(src.Int63())
}

func RandomString(n int) string {
	b := make([]byte, n)
	// A src.Int63() generates 63 random bits, enough for letterIdxMax characters!
	for i, cache, remain := n-1, src.Int63(), letterIdxMax; i >= 0; {
		if remain == 0 {
			cache, remain = src.Int63(), letterIdxMax
		}
		if idx := int(cache & letterIdxMask); idx < len(letterBytes) {
			b[i] = letterBytes[idx]
			i--
		}
		cache >>= letterIdxBits
		remain--
	}

	return *(*string)(unsafe.Pointer(&b))
}

func HashPassword(password string) (string, error) {
	hash := sha3.New512()
	_, err := hash.Write([]byte(password))
	if err != nil {
		return "", err
	}

	bytes := hash.Sum(make([]byte, 512)[:0])
	return base64.StdEncoding.EncodeToString(bytes), err
}
