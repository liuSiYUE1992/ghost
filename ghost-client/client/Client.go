package client

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"ghost-client/server"
	"ghost-client/utils"
	"io"
	"io/ioutil"
	"net"
	"os"
	"strings"
	"time"
)

type TcpClient struct {
	ProxyArray []*Proxy
}

type ProxyData struct {
	Port   string `json:"port"`
	Target string `json:"target"`
}

type Proxy struct {
	Port       string
	Ip         string
	Target     string
	Conn       net.Conn
	ServerConn net.Conn
}

func (cli *TcpClient) ImportProxy() {
	path, err := os.Getwd()
	if err != nil {
		fmt.Println("pwd is err", err)
		return
	}
	file, err := ioutil.ReadFile(path + "/client_proxy.conf")
	if err != nil {
		fmt.Println("read proxylist.txt error err = ", err)
		return
	}
	scanner := bufio.NewScanner(bytes.NewReader(file))
	for scanner.Scan() {
		if scanner.Text() != "" {
			s := strings.Split(scanner.Text(), ";")
			if len(s) == 2 {
				fmt.Println("导入穿透", s[0], "的", s[1], "端口")
				cli.ProxyArray = append(cli.ProxyArray, &Proxy{
					Ip:   s[0],
					Port: s[1],
				})
			}
		}
	}
	fmt.Println("import proxy success count ", len(cli.ProxyArray))
}

func (pro *Proxy) ClientConnection() {
	_ = pro.Conn.SetReadDeadline(time.Now().Add(time.Second * 5))
	for {
		buf := make([]byte, 10240)
		_, err := pro.Conn.Read(buf)
		if err != nil {
			if err == io.EOF {
				pro.Conn = utils.Bind(pro.Ip, pro.Port)
				continue
			}
			_ = pro.Conn.SetReadDeadline(time.Now().Add(time.Second * 3))
			continue
		}
		data := &ProxyData{
			Port:   pro.Port,
			Target: pro.Target,
		}
		bufs, err := json.Marshal(data)
		if err != nil {
			fmt.Println(err)
		}
		sm := &server.ServerMessage{
			Flag: 9675,
			Typ:  utils.CLIENT_PUBLISH,
			Data: bufs,
		}
		_, err = pro.ServerConn.Write(sm.Encode())
		if err != nil {
			fmt.Println(err)
		}

	}
}

func (pro *Proxy) Send(data []byte) {
	if pro.Conn == nil {
		fmt.Println("wait target ack.. ", pro.Port)
		return
	}
	_, err := pro.Conn.Write(data)
	if err != nil {
		fmt.Println("send to local service err", err)
	}
}
