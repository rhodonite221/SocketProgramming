// SocketProgramming.cpp : 이 파일에는 'main' 함수가 포함됩니다. 거기서 프로그램 실행이 시작되고 종료됩니다.
//
#include "stdafx.h"
#include <iostream>
#include <stdio.h>
//소켓을 사용하기 위해선 라이브러리를 링크 걸어줘야 한다. 헤더에 선언한 기능들을 cpp에 구현하고, cpp에 구현된 내용들이 라이브러리화 된다.
//즉, WinSock2.h에 선언한 기능들을 사용하겠다고, ws2_32라이브러리를 링크 걸어주는 것이다.
#include <WinSock2.h>
#pragma comment(lib, "ws2_32")

#define PORT 4578
#define PACKET_SIZE 1024

int main()
{
    std::cout << "Hello World!\n";

    //Windwos의 소켓 초기화 정보를 저장하기위한 구조체. 이미 선언되어 있는 구조체이다.
    WSADATA wsaData;
    //소켓버전, WSADATA구조체 주소. 이 함수를 호출해서 윈도우즈의 어느 소켓을 활용할 것인지 알려준다. 2.2 버전을 사용할 것임.
    //WORD는 unsigend short 타입을 typedef 해놓은 것. 2.2는 실수이므로 MAKEWORD 매크로로 만들어준다.
    WSAStartup(MAKEWORD(2,2), &wsaData);

    //SOCKET은 핸들이다. 핸들이란 운영체제가 관리하는 커널오브젝트의 한 종류
    //윈도우를 생성해도 해당 윈도우의 핸들이 생성되고 운영체제가 그 핸들을 이용해서 어떤 프로그램인지를 구분한다던지 함.
    SOCKET hListen;
    //TCP 소켓은 크게 2가지로 나뉜다. 첫번째는 다른 컴퓨터로부터의 접속 승인 소켓, 다른 컴퓨터와 연결된 소켓.
    //PF_INET은 IPv4, SOCK_STREAM을 넣어주면 연결지향형 소켓, 세번째는 protocol 지정. IPPROTO_TCP는 TCP를 사용하겠다고 지정해주는 것.
    hListen = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);

    //위 소켓의 구성요소 지정.
    //Internet Address Family에서 windows 소켓에서 소켓을 연결할 로컬 또는 원격 주소를 지정하는데 사용
    //즉, 주소정보를 담아두는 구조체.
    SOCKADDR_IN tListenAddr = {};
    tListenAddr.sin_family = AF_INET;   //AF_INET이어야 함. UDP TCP를 쓰려면
    tListenAddr.sin_port = htons(PORT); //PORT번호를 결정함. host to network short. 이 함수를 거치면 무조건 빅 엔디안 방식으로 데이터를 변환
    tListenAddr.sin_addr.s_addr = htonl(INADDR_ANY);  //서버는 현재 동작되는 컴퓨터의 IP주소 INADDR_ANY는 현재 동작 컴퓨터의 IP주소. s_addr은 IPv4를 의미함.

    bind(hListen, (SOCKADDR*)&tListenAddr, sizeof(tListenAddr));    //소켓에 주소정보를 전달한다. 소켓을 접속 대기 상태로 만들어줌. 첫번째는 소켓, 두번째는 주소정보의 구조체, 세번째는 구조체의 크기
    listen(hListen, SOMAXCONN); //listen 함수는 연결을 수신하는상태로 소켓의 상태를 변경한다. 소켓을 접속 대기 상태로 만들어준다. SOMAXCONN은 한꺼번에 요청 가능한 최대 접속승인 수

    //클라이언트 소켓 생성 및 정보를 담을 구조체 생성 및 값 할당, 클라이언트가 접속 요청하면 승인해주는 역할.
    SOCKADDR_IN tClntAddr = {};
    int iClntSize = sizeof(tClntAddr);
    //이 함수는 동기화 방식으로 동작. 요청이 들어가기 전까지 이 함수는 안빠져나온다.
    SOCKET hClient = accept(hListen, (SOCKADDR*)&tClntAddr, &iClntSize);

    //클라이언트쪽 정보를 수신하기 위해 기존의 정의해둔 패킷 크기만큼 버퍼를 생성한다.
    char cBuffer[PACKET_SIZE] = {};
    recv(hClient, cBuffer, PACKET_SIZE, 0); //소켓, 수신정보를 담을 배열주소, 배열의 크기, flag
    printf("Recv Msg : %s\n", cBuffer); // 출력

    char cMsg[] = "Server Send";
    send(hClient, cMsg, strlen(cMsg), 0);   //서버가 메시지를 클라이언트측에 전달.

    closesocket(hClient);
    closesocket(hListen);


    //소켓 활용는 WSAStartup과 WSACleanup 사이에 작성해야한다. 생성자와 소멸자 같은 개념.
    //WSAStartup을 하면서 지정한 내용을 지워준다.
    WSACleanup();
    return 0;
}

// 프로그램 실행: <Ctrl+F5> 또는 [디버그] > [디버깅하지 않고 시작] 메뉴
// 프로그램 디버그: <F5> 키 또는 [디버그] > [디버깅 시작] 메뉴

// 시작을 위한 팁: 
//   1. [솔루션 탐색기] 창을 사용하여 파일을 추가/관리합니다.
//   2. [팀 탐색기] 창을 사용하여 소스 제어에 연결합니다.
//   3. [출력] 창을 사용하여 빌드 출력 및 기타 메시지를 확인합니다.
//   4. [오류 목록] 창을 사용하여 오류를 봅니다.
//   5. [프로젝트] > [새 항목 추가]로 이동하여 새 코드 파일을 만들거나, [프로젝트] > [기존 항목 추가]로 이동하여 기존 코드 파일을 프로젝트에 추가합니다.
//   6. 나중에 이 프로젝트를 다시 열려면 [파일] > [열기] > [프로젝트]로 이동하고 .sln 파일을 선택합니다.
