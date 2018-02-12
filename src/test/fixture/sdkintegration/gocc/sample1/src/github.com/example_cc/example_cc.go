/*
Copyright IBM Corp. 2016 All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package main

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

// SimpleChaincode example simple Chaincode implementation
type SimpleChaincode struct {
}

type Asset struct {
	UUID         string `json:"uuid"`
	SerialNumber string `json:"serialNumber"`
	AssetType    string `json:"assetType"`
	OwnerName    string `json:"ownerName"`
	Description  string `json:"description"`
}

// Init initializes the chaincode state
func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("########### Init ###########")
	asset := Asset{
		UUID:         "14a12ef0-9409-4872-9341-9ab003059ce9",
		SerialNumber: "3SZ1W278EJ8",
		AssetType:    "VEHICLE",
		OwnerName:    "THOMAS ALVA EDISON",
		Description:  "1908 FORD MODEL T\nCOLOR: ANYONE, AS LONG AS IT IS BLACK\nINTERIORS COLOR: BROWN LEATHER.\nENGINE: 2.9 L FOUR CYLINDERS\nENGINE NUMBER: 000015\nMADE IN: UNITED STATES\nBUYER NAME: THOMAS EDISON\nADDRESS: 345 Llewellyn Park, New Jersey, United States, USA.\nSELLER NAME: HENRY FORD",
	}
	jsonBlob, _ := json.Marshal(asset)
	err := stub.PutState(asset.UUID, jsonBlob)

	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)

}

// Invoke makes payment of X units from A to B
func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("########### Invoke ###########")
	function, args := stub.GetFunctionAndParameters()

	if function != "invoke" {
		return shim.Error("Unknown function call")
	}

	if len(args) < 2 {
		return shim.Error("Incorrect number of arguments. Expecting at least 2")
	}

	fn := args[0]
	switch fn {
	case "create":
		return shim.Error("Not yet implemented")
	case "query":
		return t.query(stub, args)
	case "update":
		return shim.Error("Not yet implemented")
	case "delete":
		return shim.Error("Not yet implemented")
	default:
		return shim.Error("Unknown action, check the first argument, must be one of 'create', 'query', 'update' or 'delete'")
	}
}

func (t *SimpleChaincode) query(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("########### Query ###########")
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting name of the person to query")
	}

	id := args[1]

	assetBlob, err := stub.GetState(id)
	if err != nil {
		return shim.Error("Failed to get state")
	}
	if assetBlob == nil {
		return shim.Error("Not found!")
	}

	jsonResp := "{\"id\":\"" + id + "\",\"val\":\"" + string(assetBlob) + "\"}"

	a1 := Asset{}
	json.Unmarshal([]byte(assetBlob), &a1)
	fmt.Printf("-> %+v\n", a1)
	fmt.Printf("Query Response:%s\n", jsonResp)
	return shim.Success(assetBlob)
}

func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}
