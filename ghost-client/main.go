package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"ghost-client/client"
	"ghost-client/server"
	"ghost-client/utils"
	"io"
	"net"
	"time"
)

var tcpClient = &client.TcpClient{}

func pong(targetName string, coreThread net.Conn) {
	fmt.Println("收到来自EasyProxyServer的心跳...")
	data := &client.ProxyData{
		Target: targetName,
	}
	marshaler, err := json.Marshal(data)
	if err != nil {
		panic(err)
	}
	sm := &server.ServerMessage{
		Typ:  utils.PONG,
		Data: marshaler,
	}

	_, _ = coreThread.Write(sm.Encode())
}

func waiting(ip, port, targetName string, coreThread net.Conn, tc *client.TcpClient) {
	for {
		buf := make([]byte, 10240)
		offset, err := coreThread.Read(buf)
		if err != nil {
			if err == io.EOF {
				fmt.Println("与Server断开连接，尝试重连...")
				for {
					coreThread = utils.Bind(ip, port)
					if coreThread != nil {
						initProxy(targetName, coreThread)
						break
					}
					time.Sleep(3 * time.Second)
				}
			}
			continue
		}
		if offset > 12 {
			sm := &server.ServerMessage{}
			s := sm.Decode(buf[:offset])
			pd := &client.ProxyData{}
			if s.Typ == utils.PING {
				pong(targetName, coreThread)
				continue
			}

			for _, data := range tc.ProxyArray {
				if data.Port == pd.Port {
					switch s.Typ {
					case utils.CONN_ACK:
						if data.Conn == nil {
							go reConn(data)
						} else {
							go data.ClientConnection()
						}
						//pass
					case utils.PUBLISH:
						if data.Conn == nil {
							reConn(data)
						}
						data.Send(s.Data)
					}
				}
			}

		}
	}
}

func reConn(data *client.Proxy) {
	for {
		clientConn := utils.Bind(data.Ip, data.Port)
		if clientConn != nil {
			data.Conn = clientConn
			data.ClientConnection()
			break
		} else {
			time.Sleep(1 * time.Second)
		}
	}
}
func main() {
	ip := flag.String("ip", "127.0.0.1", "EasyProxyServer host")
	port := flag.String("port", "4396", "EasyProxyServer port")
	name := flag.String("name", "client", "123")
	flag.Parse()
	//init server
	coreThread := utils.Bind(*ip, *port)
	tcpClient.ImportProxy()
	targetName := *name

	// core thread
	initProxy(targetName, coreThread)
	// waiting main thread
	waiting(*ip, *port, targetName, coreThread, tcpClient)

}

func initProxy(targetName string, coreThread net.Conn) {

	for _, proxy := range tcpClient.ProxyArray {
		proxy.Target = targetName
		proxy.ServerConn = coreThread
		register := &server.Register{
			Name: targetName,
			Port: proxy.Port,
			Ip:   proxy.Ip,
		}
		marshaler, _ := json.Marshal(register)
		sm := &server.ServerMessage{
			Flag: 4396,
			Typ:  utils.CONN,
			Data: marshaler,
		}
		_, err := coreThread.Write(sm.Encode())
		if err != nil {
			fmt.Println(err)
			continue
		}
	}
}
