import yaml, sys, json

with open(sys.argv[1], 'r') as f:
    d = yaml.load(f)

with open(sys.argv[2], 'r') as f:
    trans = yaml.load(f)

type_table = {}

for (k, v) in d.iteritems():
    type_table[k] = v["type"]

def dump_trait(target, trait_host, trait_name):
    print r'{0}.put("{1}", "{2}", new InlineTranslator({3}));'.format(
        "TRAITS",
        target, trait_name, json.dumps(trans[trait_host][trait_name])
    )

def translate_multine(multi_line):
    accum = []
    for l in multi_line.split("\n"):
        accum.append(json.dumps(l + "\n"))
    return " + \n".join(accum)

def dump_attr(target, attr_host, attr_name, value_type):
    if value_type == "STRING":
        java_type = "String"
    else:
        java_type = type_table[value_type]

    if attr_name.startswith("*"):
        attr_name = attr_name[1:]
        java_type = "java.util.List<%s>" % java_type

    attr_generator = trans[attr_host][attr_name]
    if "$BLOCK" in attr_generator:
        input_type = type_table[target]
        print r'{0}.put("{1}", "{2}", new BlockTranslator("{3}", "{4}", {5}, "{6}"));'.format(
            "ATTR",
            target,
            attr_name,
            input_type,
            java_type,
            translate_multine(attr_generator),
            value_type,
        )
    else:
        print r'{0}.put("{1}", "{2}", new InlineTranslator({3}, "{4}"));'.format(
            "ATTR",
            target,
            attr_name,
            json.dumps(attr_generator),
            value_type,
        )

def dump_attr_list(target, attr_host):
    for attr in d[attr_host]["attributes"]:
        if type(attr) == str:
            dump_attr(target, attr_host, attr, attr)
        else:
            dump_attr(target, attr_host, attr[0], attr[1])

def dump_value_traits(v):
    it = v
    while True:
        if "traits" in d[it]:
            for t in d[it]["traits"]:
                dump_trait(v, it, t)
        if "extends" in d[it]:
            it = d[it]["extends"]
        else:
            return

transitive_closure = dict()
upper_closure = dict()

def dump_value_attr(v):
    dump_attr_list(v, v)
    print r'TYPE_TABLE.put("{0}", "{1}");'.format(v, type_table[v])
    it = v
    up_clos = set()
    while True:
        if it not in transitive_closure:
            transitive_closure[it] = set()
        transitive_closure[it].add(v)
        if "extends" in d[it]:
            it = d[it]["extends"]
            up_clos.add(it)
        else:
            break
    upper_closure[v] = up_clos

print "static {\n// START AUTO GENERATED CODE"
for k in d.iterkeys():
    dump_value_attr(k)
    dump_value_traits(k)
for k in d.iterkeys():
    for reach in (transitive_closure[k] | upper_closure[k]):
        ty = d[reach]["type"]
        for a in d[reach]["attributes"]:
            if type(a) == list:
                a = a[0]
            if a.startswith("*"):
                a = a[1:]
            print r'AVAIL_ATTR.put("{0}", "{1}", "{2}");'.format(k, a, reach)
        for t in d[reach].get("traits", []):
            print r'AVAIL_TRAITS.put("{0}", "{1}", "{2}");'.format(k, t, reach)
print "}"
