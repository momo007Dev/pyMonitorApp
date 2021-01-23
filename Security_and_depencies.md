# Raspberry Pi temperature and humidity monitoring

---

## Table of content

- [Main Raspberry Pi configuration](#main-raspberry-pi-configuration)
  * [Changing default username and password](#changing-default-username-and-password)
  * [Prompt password when using sudo rights](#prompt-password-when-using-sudo-rights)
  * [Set Root password](#set-root-password)
- [Internet and access-point configuration](#internet-and-access-point-configuration)
  * [Scan for wifi networks](#scan-for-wifi-networks)
  * [Configuring the wifi network](#configuring-the-wifi-network)
  * [Reconfigure the interface](#reconfigure-the-interface)
- [Package Update](#package-update)
- [Security](#security)
  * [Nmap port-scanner instal](#nmap-port-scanner-instal)
  * [Nmap port check](#nmap-port-check)
  * [Enabling SSH](#enabling-ssh)
  * [SSH Security and configuration](#ssh-security-and-configuration)
  * [UFW Firewall](#ufw-firewall)
* [Installing python3, pip3 and electronic modules](#installing-python3,-pip3-and-electronic-modules)
* [Making some aliases](#Making-some-aliases)
* [Static IP](#static-ip)

---

## Project Description

This project is divided into 3 parts :

1. The sensor (attached to the raspberry pi) :
	* Will give information about **`temperature`**
	* Will give information about **`humidity`**

2. The backend API routes (using flask) :
	* I'm using flask for an easy and quick backend.
	* We have a few routes, like for example **`api/pi_data`** that gives data like pi **temperature**, **ram used** and **disk space**.
	* More coming soon !

3. An android mobile app

---

### Main Raspberry Pi configuration

User : toto

---

1. #### Changing default username and password

```bash
sudo adduser toto
sudo usermod -a -G adm,dialout,cdrom,sudo,audio,video,plugdev,games,users,input,netdev,gpio,i2c,spi alice

sudo pkill -u pi
sudo deluser pi
sudo deluser -remove-home pi
```

---

2. #### Prompt password when using sudo rights

```bash
sudo visudo /etc/sudoers.d/010_pi-nopasswd
toto ALL=(ALL) PASSWD: ALL
```

---

3. #### Set Root password

> ***PS : sudo -i = root***

```bash
sudo passwd root
PWD = Passw0rd!
```

---

### Internet and access-point configuration

1. #### Scan for wifi networks


```bash
sudo iwlist wlan0 scan
```

2. #### Configuring the wifi network

> PS : We can configure multiple wifi network in this file

```bash
sudo nano /etc/wpa_supplicant/wpa_supplicant.conf
```

```bash
network={
    ssid="test"
    psk="myPassword"
}
```

3. #### Reconfigure the interface

```bash
wpa_cli -i wlan0 reconfigure
```

---

### Package Update

```bash
sudo apt update
sudo apt upgrade
```

---

### Security

1. #### Nmap port-scanner install

```bash
sudo apt install nmap
```

---

2. #### Nmap port check

```bash
nmap localhost
```

---

3. #### Enabling SSH

```bash
sudo systemctl enable ssh
sudo systemctl start ssh
```
> PS : To connect to pi : **`ssh user@ip`**

---

4. #### SSH Security and configuration

* Client (=my pc)

```bash
ssh-keygen -t rsa
cd .ssh/
ssh-copy-id -i id_rsa.pub toto@ip
ssh toto@ip
```

* Server (=rasp pi)

```bash
sudo nano /etc/ssh/sshd_config
```

> **Modify thoses 3 lines to enable only key based authentification**

```bash
ChallengeResponseAuthentication no
PasswordAuthentication no
UsePAM no
```

---

5. #### UFW Firewall

> **Fix "ERROR: Couldn't determine iptables version"**

```bash
sudo update-alternatives --set iptables /usr/sbin/iptables-legacy
sudo reboot
```

* Create a little script to enable all firewall rules

```bash
touch ufw.sh && chmod +x ufw.sh
```

```bash
#!/bin/bash

R="\033[0;31m"
B="\033[0;34m"
O="\033[0;33m"
N="\033[0m"

sudo sed -i 's/IPV6=yes/IPV6=no/' /etc/default/ufw
sudo ufw default deny incoming
sudo ufw default allow outgoing
printf "Basic configuration $R done !$N\n"
sleep 1
echo ""
printf "$B Allowing $R ports $N:\n"
echo "----------------"
printf "$R 22 $N  $B(SSH)$N\n"
sudo ufw allow 22
sleep 1
printf "Done\n"
sudo ufw disable
sudo ufw enable
sudo ufw status
echo ""
printf "$R Nmap $B check :$N\n"
nmap localhost
```

---

### Installing python3, pip3 and electronic modules

* **python 3** :

```bash
sudo apt install python3
```

* **pip3** :

```bash
curl -sS https://bootstrap.pypa.io/get-pip.py | sudo python3
```

* **flask** (for backend server) :

```bash
pip3 install flask
```

* **Electronic modules** :

```bash
sudo apt-get install python3-gpiozero python-gpiozero
```

---------------------
### Making some aliases

```bash
sudo nano .bash_aliases
```

```bash
alias start='python3 /home/jodie/pymonitorapp/api.py'
alias c='clear'
alias ex='exit'
alias re='reboot now'
```

---

### Static IP

```bash
sudo nano /etc/dhcpcd.conf
```

```bash
interface wlan0
static ip_address=(ip)/(CIDR)
static routers=(ip_gateway)
static domain_name_servers=(ip_gateway) 8.8.8.8
```