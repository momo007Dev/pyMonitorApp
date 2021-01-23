# Backend

## The code

* **`api.py`**

```python
import flask, os, json, socket
from flask import request, jsonify

app = flask.Flask(__name__)
#app.config["DEBUG"] = True

data = {
    "temperature" : "",
    "ram_space" : {
        "total_space" : "",
        "available_space" : ""
    },
    "disk_space" : {
        "total_space" : "",
        "available_space" : ""
    }
}

def get_pi_temp(data):
    output = os.popen("vcgencmd measure_temp")
    line = output.readline()
    if (not len(line) > 15):
        temp = line[5:-3]
        data["temperature"] = temp
        y = jsonify(data)
        return y

def get_disk_space(data):
    df = os.popen("df -h /")
    i = 0
    while True:
        i = i + 1
        line = df.readline()
        if i==2:
            line_tab = line.split()[0:6]
            total_space = line_tab[1]
            available_space = line_tab[3]
            data["disk_space"]["total_space"] = total_space
            data["disk_space"]["available_space"] = available_space
            y = jsonify(data)
            return y

def get_ram_space(data):
    output = os.popen("free -h |grep Mem")
    line = output.readline()
    line_tab = line.split()
    total_ram = line_tab[1]
    avail_ram = line_tab[6]
    data["ram_space"]["total_space"] = total_ram
    data["ram_space"]["available_space"] = avail_ram
    y = jsonify(data)
    return y

def get_ip_addr():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 0))
        if (s.getsockname()[0] == "127.0.0.1"):
            s.close()
            return -1
        else:
            ip = s.getsockname()[0]
            s.close()
            return ip
    except OSError:
        s.close()
        return -1

@app.route('/api/sensor_data', methods=['GET'])
def get_info_from_sensor():
    output = os.popen('./test2.bash') # You'll need to create this file !
    pi_data = output.read()
    temp = pi_data.split()[0][5:-1]
    hum = pi_data.split()[1][9:-1]
    return jsonify({'humidity': hum, 'temperature': temp})

@app.route('/api/pi_data', methods=['GET'])
def api_get_pi_data():
    get_pi_temp(data)
    get_ram_space(data)
    get_disk_space(data)
    return jsonify(data)


# Only Starts server if device has an IP

my_ip = get_ip_addr()

if (my_ip == -1):
    print("No internet")
    print("Server not started")

else:
    app.run(host=my_ip)
 
```
