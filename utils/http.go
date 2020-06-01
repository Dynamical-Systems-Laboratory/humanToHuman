package utils

import (
	_ "image/jpeg"
	_ "image/png"
	"io/ioutil"
	"net/http"
	"net/url"
	"strconv"
	"strings"
)

type QueryMap map[string]string

type response struct {
	Status        uint64
	StatusMessage string
	Body          []byte
}

type Response struct {
	Status        uint64
	StatusMessage string
	Body          string
}

func SendRequest(method, urlString string, queryParams QueryMap, formValues url.Values) (Response, error) {
	reqURL, err := url.Parse(urlString)
	if err != nil {
		return Response{}, err
	}

	q := reqURL.Query()
	for k, v := range queryParams {
		q.Add(k, v)
	}
	reqURL.RawQuery = q.Encode()

	var resp *http.Response
	if method == http.MethodPost {
		resp, err = http.PostForm(reqURL.String(), formValues)
	} else {
		var req http.Request
		req.URL = reqURL
		req.Method = method
		resp, err = http.DefaultClient.Do(&req)
	}

	if err != nil {
		return Response{}, err
	}
	defer resp.Body.Close()

	statusMessage := strings.SplitN(resp.Status, " ", 2)
	statusCode, err := strconv.ParseUint(statusMessage[0], 10, 64)
	if err != nil {
		return Response{}, err
	}

	responseBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return Response{}, err
	}
	dataString := strings.TrimSpace(string(responseBytes))

	return Response{statusCode, statusMessage[1], dataString}, nil
}

func sendRequest(method, urlString string, queryParams QueryMap,
	formValues url.Values) response {
	reqURL, err := url.Parse(urlString)
	IFailIf(err, "failed to parse endpoint")

	q := reqURL.Query()
	for k, v := range queryParams {
		q.Add(k, v)
	}
	reqURL.RawQuery = q.Encode()

	var resp *http.Response
	if method == http.MethodPost {
		resp, err = http.PostForm(reqURL.String(), formValues)
	} else {
		var req http.Request
		req.URL = reqURL
		req.Method = method
		resp, err = http.DefaultClient.Do(&req)
	}

	IFailIf(err, "Failed to perform "+method+" for url="+urlString)
	defer resp.Body.Close()

	statusMessage := strings.SplitN(resp.Status, " ", 2)
	statusCode, err := strconv.ParseUint(statusMessage[0], 10, 64)
	IFailIf(err, "failed to parse status code")

	responseBytes, err := ioutil.ReadAll(resp.Body)
	IFailIf(err, "bytes failed to read")

	return response{statusCode, statusMessage[1], responseBytes}
}
