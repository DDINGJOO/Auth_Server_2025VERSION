# Kubernetes Master Node 설치 가이드

Ubuntu Server 22.04 LTS 기준

---

## 1. 네트워크 정보 확인

```bash
ip route show default
<응답예시>
default via 192.168.227.2 dev enp2s0 proto dhcp src 192.168.227.135 metric 100

 
ip -4 addr show
<응답 예시>
2: enp2s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    inet 192.168.227.135/24 metric 100 brd 192.168.227.255 scope global dynamic enp2s0
       valid_lft 1679sec preferred_lft 1679sec
```

출력 결과에서 다음 정보 확인:

위치를 보고 본인 세팅으로 바꿔주세요

- 게이트웨이 주소 192.168.227.2
- 이더넷 이름 enp2s0
- 사용할 아이디 : 192.168.227.135/24 범위 내에서 설정

---

## 2. 고정 IP 설정

### 네트워크 설정 파일 수정

```bash
sudo vi /etc/netplan/00-installer-config.yaml
```

다음 내용으로 수정
위에서 확인한 정보로 변경

```yaml
network:
  ethernets:
    enp2s0: < 이더넷 이름
      dhcp4: false
      addresses:
        - 192.168.227.100/24 < 내가 쓸 아이피
      routes:
        - to: default
          via: 192.168.227.2 < 게이트 웨이 주소
      nameservers:
        addresses:
          - 8.8.8.8
          - 8.8.4.4
  version: 2
```

### 네트워크 설정 적용

```bash
sudo netplan apply
```

### 네트워크 확인

```bash
ip a
ping -c 3 8.8.8.8
ping -c 3 <게이트웨이>
```

---

## 3. 시스템 업데이트

```bash
sudo apt-get update
sudo apt-get upgrade -y
```

---

## 4. 호스트명 설정

```bash
sudo hostnamectl set-hostname master
```

### /etc/hosts 파일 수정

```bash
sudo vi /etc/hosts
```

다음 내용 추가:

```
192.168.1.100   master
192.168.1.101   worker1
192.168.1.102   worker2
```

### 호스트 간 통신 확인

```bash
ping -c 2 master
```

---

## 5. Swap 비활성화

```bash
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab
free -h
```

---

## 6. 커널 모듈 로드

```bash
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

lsmod | grep br_netfilter
lsmod | grep overlay
```

---

## 7. 커널 파라미터 설정

```bash
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

---

## 8. 필수 패키지 설치

```bash
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common gnupg lsb-release
```

---

## 9. containerd 설치

### Docker 저장소 추가

```bash
sudo mkdir -p /etc/apt/keyrings

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu jammy stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

### containerd 설치

```bash
sudo apt-get update
sudo apt-get install -y containerd.io
```

---

## 10. containerd 설정

```bash
sudo mkdir -p /etc/containerd

containerd config default | sudo tee /etc/containerd/config.toml

sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml

sudo systemctl restart containerd
sudo systemctl enable containerd
sudo systemctl status containerd
```

q를 눌러 종료

---

## 11. Kubernetes 설치

### Kubernetes 저장소 추가

```bash
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list
```

### kubeadm, kubelet, kubectl 설치

```bash
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
sudo systemctl enable kubelet
```

### 설치 확인

```bash
kubeadm version
kubelet --version
kubectl version --client
```

---

## 12. 재부팅

```bash
sudo reboot
```

---

## 13. 클러스터 초기화

재부팅 후 로그인

### 네트워크 및 Swap 확인

```bash
ip a
free -h
```

### 클러스터 초기화

```bash
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=<본인이 설정한 아이피>
```

초기화 완료 후 출력되는 join 토큰을 반드시 복사해두세요

---

## 14. kubectl 설정

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

### kubectl 동작 확인

```bash
kubectl get nodes
kubectl get pods -A
```

// 동장확인용 -> 명령어가 실행만된다면 진행

---

## 15. Flannel CNI 설치

```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

### 설치 확인

```bash
kubectl get pods -A
```

1-2분 기다린 후:

```bash
kubectl get nodes
kubectl get pods -A -o wide
```

---

## 16. Worker 노드 조인 대기

Worker 노드에서 join 명령어 실행 후 다음 명령어로 확인:

```bash
kubectl get nodes
kubectl get nodes -o wide
```

---

## 문제 해결

### Join 명령어를 잊어버렸을 때

```bash
kubeadm token create --print-join-command
```

### 테스트 Pod 배포

```bash
kubectl run nginx --image=nginx --port=80
kubectl get pods
kubectl get pods -o wide
```
