import pickle, sys, tempfile, subprocess

address_map = pickle.loads(sys.argv[1])
print address_map

lines = []
with open('/etc/hosts', 'r') as f:
    for l in f:
        found = False
        for (k, v) in address_map.iteritems():
            if k in l:
                lines.append(v + " " + k + "\n")
                found = True
                break
        if not found:
            lines.append(l)
new_host_blob = "".join(lines)
host_temp = tempfile.NamedTemporaryFile()
print >> host_temp, new_host_blob,
host_temp.flush()
subprocess.call(["diff", "-u", "/etc/hosts", host_temp.name])
resp = raw_input("Apply the above changes? [y/n] ")
if resp == "y":
    with open("/etc/hosts", "w") as f:
        print >> f, new_host_blob,
else:
    print "Byyyye"
