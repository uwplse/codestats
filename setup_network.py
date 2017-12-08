import subprocess, json, pickle

target_sg = "sg-f620fe8a"

my_ip = subprocess.check_output(["curl", "-s", "https://api.ipify.org"])
ip_perms = json.loads(subprocess.check_output(["aws", "ec2", "describe-security-groups", "--group-ids", target_sg]))["SecurityGroups"][0]["IpPermissions"]
found = False
for ip_perm in ip_perms:
    for ip_range in ip_perm["IpRanges"]:
        if "CidrIp" not in ip_range:
            continue
        if ip_range["CidrIp"] == my_ip + "/32":
            found = True
            break
    if found:
        break
if not found:
    print "IP permissions not found, adding now..."
    subprocess.call(["aws", "ec2", "authorize-security-group-ingress", "--protocol", "tcp", "--port", "0-65535", "--cidr", my_ip + "/32", "--group-id", target_sg])
d = json.loads(subprocess.check_output(["aws", "ec2", "describe-instances"]))
address_map = {}
for res in d["Reservations"]:
    for inst in res["Instances"]:
        if inst["State"]["Name"] != "running":
            continue
        address_map[inst["PrivateDnsName"]] = inst["PublicIpAddress"]
print "Lauching host editor with sudo..."
subprocess.check_call(["sudo", "python", "./edit_hosts.py", pickle.dumps(address_map)])
