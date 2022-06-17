package server

import (
	"bytes"
	"ghost-client/utils"
)

type ServerMessage struct {
	Flag   int32
	Typ    int32
	Length int32
	Data   []byte
	Crc    int32
}

type Transfer interface {
	Encode() []byte
	Decode() *ServerMessage
}

func (sm *ServerMessage) Encode() []byte {
	dataBuff := bytes.NewBuffer([]byte{})
	dataBuff.Write(utils.IntToBytes(4379))
	dataBuff.Write(utils.IntToBytes(int(sm.Typ)))
	var size = 0
	if sm.Data != nil {
		size += len(sm.Data)
	}

	dataBuff.Write(utils.IntToBytes(size))
	if sm.Data != nil {
		dataBuff.Write(sm.Data)
	}
	dataBuff.Write(utils.IntToBytes(0))
	return dataBuff.Bytes()
}

func (sm *ServerMessage) Decode(byt []byte) *ServerMessage {
	sm.Flag = utils.BytesToInt(byt[0:4])
	if sm.Flag == 4379 {
		sm.Typ = utils.BytesToInt(byt[4:8])
		sm.Length = utils.BytesToInt(byt[12:16])
		sm.Data = byt[sm.Length+16:]
	}
	return sm
}

func (sm *ServerMessage) GetFlag() int32 {
	return sm.Flag
}
func (sm *ServerMessage) GetTyp() int32 {
	return sm.Typ
}
func (sm *ServerMessage) GetLength() int32 {
	return sm.Length
}
func (sm *ServerMessage) GetData() []byte {
	return sm.Data
}
