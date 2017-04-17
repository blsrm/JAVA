#!/bin/sh

# Simple script to obtain host info from Linux systems
# Script is divided into sections to match discovery methods

os=`uname -s`
if [ "$os" != "Linux" ]; then
    echo This script must be run on Linux
    exit 1
fi

# Set PATH
PATH=/bin:/usr/bin:/sbin:/usr/sbin:/usr/local/sbin:/usr/local/bin
export PATH

# Initialisation
tw_locale=`locale -a | grep -i en_us | grep -i "utf.*8" | head -n 1 2>/dev/null`

LANGUAGE=""
if [ "$tw_locale" != "" ]; then
    LANG=$tw_locale
    LC_ALL=$tw_locale
else
    LANG=C
    LC_ALL=C
fi
export LANG LC_ALL

# bash 4 added command_not_found_handle called when a command is unavailable.
# PackageKit-command-not-found RHEL and Fedora ask to install the package
# making discovery stall. Undefine function to stop this.
unset command_not_found_handle


# Stop alias commands changing behaviour.
unalias -a

# Insulate against systems with -u set by default.
set +u

if [ -w /tmp ] 
then
    # use a /tmp file to capture stderr
    TW_CAPTURE_FILE=/tmp/tideway_status_$$
    export TW_CAPTURE_FILE
    rm -f $TW_CAPTURE_FILE

    tw_capture(){
        TW_NAME=$1
        shift
        echo begin cmd_status_err_$TW_NAME >>$TW_CAPTURE_FILE
        "$@" 2>>$TW_CAPTURE_FILE
        RETURN_VAL=$?
        echo end cmd_status_err_$TW_NAME >>$TW_CAPTURE_FILE

        echo cmd_status_$TW_NAME=$RETURN_VAL >>$TW_CAPTURE_FILE
        return $RETURN_VAL
    }

    tw_report(){
        if [ -f $TW_CAPTURE_FILE ]
        then 
            cat $TW_CAPTURE_FILE 2>/dev/null
            rm -f $TW_CAPTURE_FILE 2>/dev/null
        fi
    }
else
    # can't write to /tmp - do not capture anything
    tw_capture(){
        shift
        "$@" 2>/dev/null
    }

    tw_report(){
        echo "cmd_status_err_status_unavailable=Unable to write to /tmp"
    }
fi 

# replace the following PRIV_XXX functions with one that has the path to a
# program to run the commands as super user, e.g. sudo. For example
# PRIV_LSOF() {
#   /usr/bin/sudo "$@"
# }

# lsof requires superuser privileges to display information on processes
# other than those running as the current user
PRIV_LSOF() {
 sudo "$@"
}

# This function supports running privileged commands from patterns
PRIV_RUNCMD() {
 sudo "$@"
}

# This function supports privileged cat of files.
# Used in patterns and to get file content.
PRIV_CAT() {
  cat "$@"
}

# This function supports privilege testing of attributes of files.
# Used in conjunction with PRIV_CAT and PRIV_LS
PRIV_TEST() {
  test "$@"
}

# This function supports privilege listing of files and directories
# Used in conjunction with PRIV_TEST 
PRIV_LS() {
  ls "$@"
}

# This function supports privilege listing of file systems and related
# size and usage.
PRIV_DF() {
  "$@"
}

# dmidecode requires superuser privileges to read data from the system BIOS
PRIV_DMIDECODE() {
    "$@"
}

# hwinfo requires superuser privileges to read data from the system BIOS
PRIV_HWINFO() {
    "$@"
}

# mii-tool requires superuser privileges to display any interface speed
# and negotiation settings
PRIV_MIITOOL() {
    "$@"
}

# ethtool requires superuser privileges to display any interface speed
# and negotiation settings
PRIV_ETHTOOL() {
    "$@"
}

# netstat requires superuser privileges to display process identifiers (PIDs)
# for ports opened by processes not running as the current user
PRIV_NETSTAT() {
    "$@"
}

# ss requires superuser privileges to display process identifiers (PIDs)
# for ports opened by processes not running as the current user
PRIV_SS() {
    "$@"
}

# lputil requires superuser privileges to display any HBA information
PRIV_LPUTIL() {
    "$@"
}

# hbacmd requires superuser privileges to display any HBA information
PRIV_HBACMD() {
    "$@"
}

# Xen's xe command requires superuser privileges
PRIV_XE(){
    "$@"
}

# esxcfg-info command requires superuser privileges
PRIV_ESXCFG(){
    "$@"
}

# Formatting directive
echo FORMAT Linux

# getDeviceInfo
echo --- START device_info

echo 'hostname:' `hostname 2>/dev/null`
echo 'fqdn:' `hostname --fqdn 2>/dev/null`
dns_domain=`hostname -d 2>/dev/null | sed -e 's/(none)//'`
if [ "$dns_domain" = "" -a -r /etc/resolv.conf ]; then
  dns_domain=`awk '/^(domain|search)/ {sub(/\\\\000$/, "", $2); print $2; exit }' /etc/resolv.conf 2>/dev/null`
fi
echo 'dns_domain: ' $dns_domain

nis_domain=`domainname 2>/dev/null`
if [ "$nis_domain" = "" ]; then
  nis_domain=`hostname -y 2>/dev/null`
fi
echo 'domain: ' $nis_domain | sed -e 's/(none)//'

os=""
# SuSE lsb_release does not provide service pack so prefer SuSE-release file
# However, this file is being deprecated so we will fallback to os-release
# (see below)
if [ "$os" = "" -a -r /etc/SuSE-release ]; then
    os=`cat /etc/SuSE-release | egrep -v '^#'`
fi
if [ "$os" = "" -a -x /usr/bin/lsb_release ]; then
    # We'd like to use -ds but that puts quotes in the output!
    os=`/usr/bin/lsb_release -d | cut -f2 -d: | sed -e 's/^[ \t]//'`
    if [ "$os" = "(none)" ]; then
        os=""
    elif [ "$os" != "" ]; then
        # Check to see if its a variant of Red Hat
        rpm -q oracle-logos > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            # Oracle variant
            os="Oracle $os"
        fi
    fi
fi
if [ "$os" = "" -a -r /proc/vmware/version ]; then
    os=`grep -m1 ESX /proc/vmware/version`
fi
if [ "$os" = "" -a -r /etc/vmware-release ]; then
    os=`grep ESX /etc/vmware-release`
fi
if [ "$os" = "" -a -r /etc/redhat-release ]; then
    os=`cat /etc/redhat-release`

    # Check to see if its a variant of Red Hat
    rpm -q oracle-logos > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        # Oracle variant
        os="Oracle $os"
    fi
fi
if [ "$os" = "" -a -r /etc/debian_version ]; then
    ver=`cat /etc/debian_version`
    os="Debian Linux $ver"
fi
if [ "$os" = "" -a -r /etc/mandrake-release ]; then
    os=`cat /etc/mandrake-release`
fi
if [ "$os" = "" -a -r /etc/os-release ]; then
    # Use os-release for SuSE (if SuSE-release wasn't present, above)
    os_id=`grep ID= /etc/os-release | cut -f2 '-d"'`
    if [ "$os_id" = "" ]; then
        os_id=`grep ID= /etc/os-release | cut -f2 -d=`
    fi
    if [ "$os_id" = "sles" -o "$os_id" = "opensuse" ]; then
        os=`grep PRETTY_NAME /etc/os-release | cut -f2 '-d"'`
    fi
fi
if [ "$os" = "" ]; then
    os=`uname -sr 2>/dev/null`
fi
echo 'os:' $os
echo 'os_arch:' `uname -m 2>/dev/null`

echo --- END device_info

# getHostInfo
echo --- START host_info

# The specification only allows for the physical RAM size to be given in either
# KB or MB.  [System Management BIOS (SMBIOS) Reference Specification, p.86 -
# http://www.dmtf.org/sites/default/files/standards/documents/DSP0134_2.8.0.pdf]
if [ -f /usr/sbin/dmidecode ]; then
    dmidecode_ram=`PRIV_DMIDECODE /usr/sbin/dmidecode -t 17 2>/dev/null | awk '
        /Size:/ {
            if ($3 == "kB" || $3 == "KB")
                size += $2
            else if ($3 == "MB")
                size += $2*1024
        }
        END {
            print size
        }'`
    if [ "${dmidecode_ram}" != "" ]; then
        ram="${dmidecode_ram}KB"
    fi
fi

logical_ram=`awk '/^MemTotal:/ {print $2 "KB"}' /proc/meminfo 2>/dev/null`

if [ -f /usr/sbin/esxcfg-info ]; then
    # On a VMWare ESX controller, report the *real* hardware information
    file=/tmp/tideway-hw-$$
    uuid=""
    PRIV_ESXCFG /usr/sbin/esxcfg-info --hardware > ${file} 2>/dev/null
    if [ $? -eq 0 ]; then
        ram=`grep "Physical Mem\." ${file} | sed 's/[^0-9]*//g'`B
        # For esx/esxi, we should NOT use memory from dmesg or /proc/meminfo
        # because the values are incorrect
        logical_ram=""
        uuid=`grep "BIOS UUID\." ${file}`
    fi
    rm -f ${file}

    # Get UUID as hostid if possible
    if [ "$uuid" != "" ]; then
        # Process horrid BIOS UUID format :(
        echo "$uuid" | sed -e 's/\./ /g' | awk '{
printf("hostid: ");
for(i = 3 ; i < 19; i++)
{
    printf("%02s", substr($i,3,2));
    if (i == 6 || i == 8 || i == 10 || i == 12) printf("-");
}
printf("\n");
}'
    else
        if [ -r /etc/slp.reg ]; then
            uuid=`grep hardwareUuid /etc/slp.reg | cut -f2 -d= | tr '[:upper:]' '[:lower:]' | sed -e 's/"//g'`
            if [ "${uuid}" != "" ]; then
                echo 'hostid:' ${uuid}
            fi
        fi
    fi
fi

if [ -f /opt/xensource/bin/xe ]; then
    XE=/opt/xensource/bin/xe
    # /proc/meminfo reports incorrectly for Xen domains, use "xe"
    uuid=`PRIV_XE $XE host-list | grep uuid | head -n 1 | cut -f2 -d: | awk '{print $1;}'`
    if [ $? -eq 0 ]; then
        logical_ram=`PRIV_XE $XE host-param-get uuid=$uuid param-name=memory-total`
    fi
fi

echo 'kernel:' `uname -r`

if [ "${ram}" != "" ]; then
    echo 'ram:' ${ram}
fi
if [ "${logical_ram}" != "" ]; then
    echo 'logical_ram:' ${logical_ram}
fi

# Get uptime in days and seconds
uptime | awk '
{
  if ( $4 ~ /day/ ) {
    print "uptime:", $3;
    z = split($5,t,":");
    printf( "uptimeSeconds: %d\n", ($3 * 86400) + (t[1] * 3600) + (t[2] * 60) );
  } else {
    print "uptime: 0";
    z = split($3,t,":");
    print "uptimeSeconds:", (t[1] * 3600) + (t[2] * 60);
  }
}'

# zLinux?
if [ -r /proc/sysinfo -a -d /proc/dasd ]; then
    echo "candidate_vendor[]:" `egrep '^Manufacturer:' /proc/sysinfo | awk '{print $2;}'`
    type=`egrep '^Type:' /proc/sysinfo | awk '{print $2;}'`
    model=`egrep '^Model:' /proc/sysinfo | awk '{print $2;}'`
    echo "candidate_model[]: $type-$model"
    echo "zlinux_sequence:" `egrep '^Sequence Code:' /proc/sysinfo | awk '{print $3;}'`
    echo "zlinux_vm_name:" `egrep '^VM00 Name:' /proc/sysinfo | awk '{print $3;}'`
    echo "zlinux_vm_software:" `egrep '^VM00 Control Program:' /proc/sysinfo | awk '{print $4, $5;}'`
fi

# Can we get information from the BIOS? We use lshal if available as that
# requires no superuser permissions but we attempt to run all tools as some
# can return invalid values in some cases. The system will select the "best"
# candidate from the values returned, where "best" is the first non-bogus value
if [ -x /usr/bin/lshal ]; then
    /usr/bin/lshal 2>/dev/null | sed -e 's/(string)$//g' -e "s/'//g" | awk '
    $1 ~ /(smbios\.system|system\.hardware)\.serial/ {
        sub(/.*(smbios\.system|system\.hardware).serial = */, "");
        printf("candidate_serial[]: %s\n", $0);
    }
    $1 ~ /(smbios\.system|system\.hardware)\.uuid/ {
        sub(/.*(smbios\.system|system\.hardware)\.uuid = */, "");
        printf("candidate_uuid[]: %s\n", $0);
    }
    $1 ~ /(smbios\.system|system\.hardware)\.product/ {
        sub(/.*(smbios\.system|system\.hardware)\.product = */, "");
        printf("candidate_model[]: %s\n", $0);
    }
    $1 ~ /system(\.hardware)?\.vendor/ {
        sub(/.*(system|system\.hardware)\.vendor = */, "");
        printf("candidate_vendor[]: %s\n", $0);
    }'
fi
if [ -f /usr/sbin/dmidecode ]; then
        PRIV_DMIDECODE /usr/sbin/dmidecode 2>/dev/null | sed -n '/DMI type 1,/,/^Handle 0x0/p' | awk '
    $1 ~ /Manufacturer:/ { sub(".*Manufacturer: *", ""); printf("candidate_vendor[]: %s\n", $0); }
    $1 ~ /Vendor:/ { sub(".*Vendor: *", ""); printf("candidate_vendor[]: %s\n", $0); }
    $1 ~ /Product/ && $2 ~ /Name:/ { sub(".*Product Name: *", ""); printf("candidate_model[]: %s\n", $0); }
    $1 ~ /Product:/ { sub(".*Product: *",""); printf("candidate_model[]: %s\n", $0 ); }
    $1 ~ /Serial/ && $2 ~ /Number:/ { sub(".*Serial Number: *", ""); printf("candidate_serial[]: %s\n", $0); }
    $1 ~ /UUID:/ { sub(".*UUID: *", ""); printf( "candidate_uuid[]: %s\n", $0 ); } '
fi
if [ -f /usr/sbin/hwinfo ]; then
        PRIV_HWINFO /usr/sbin/hwinfo --bios 2>/dev/null | sed -n '/System Info:/,/Info:/p' | awk '
    $1 ~ /Manufacturer:/ { sub(".*Manufacturer: *", ""); gsub("\"", ""); printf("candidate_vendor[]: %s\n", $0); }
    $1 ~ /Product:/ { sub(".*Product: *", ""); gsub("\"", ""); printf("candidate_model[]: %s\n", $0); }
    $1 ~ /Serial:/ { sub(".*Serial: *", ""); gsub("\"", ""); printf("candidate_serial[]: %s\n", $0); }
    $1 ~ /UUID:/ { sub(".*UUID: *", ""); gsub("\"", ""); printf("candidate_uuid[]: %s\n", $0); } '
fi
if [ -d /sys/class/dmi/id ]; then
    echo "candidate_model[]: " `cat /sys/class/dmi/id/product_name 2>/dev/null`
    echo "candidate_serial[]: " `PRIV_CAT /sys/class/dmi/id/product_serial 2>/dev/null`
    echo "candidate_uuid[]: " `PRIV_CAT /sys/class/dmi/id/product_uuid 2>/dev/null`
    echo "candidate_vendor[]: " `cat /sys/class/dmi/id/sys_vendor 2>/dev/null`
fi

# PPC64 LPAR?
if [ -r /proc/ppc64/lparcfg ]; then
    echo begin lparcfg:
    cat /proc/ppc64/lparcfg 2>/dev/null
    echo end lparcfg
fi
# LPAR name?
if [ -r /proc/device-tree/ibm,partition-name ]; then
    echo "lpar_partition_name:" `cat /proc/device-tree/ibm,partition-name`
fi

echo --- END host_info

# getMACAddresses
echo --- START ip_link_mac
ip -o link show 2>/dev/null
echo --- END ip_link_mac

echo --- START ifconfig_mac
ifconfig -a 2>/dev/null
echo --- END ifconfig_mac

# getIPAddresses
echo --- START ip_addr_ip
ip address show 2>/dev/null
echo --- END ip_addr_ip

echo --- START ifconfig_ip
ifconfig -a 2>/dev/null
echo --- END ifconfig_ip

# getNetworkInterfaces
echo --- START ip_link_if
ip -o link show 2>/dev/null
if [ $? -eq 0 ]; then

ETHTOOL=""
if [ -f /sbin/ethtool ]; then
    ETHTOOL=/sbin/ethtool
else
    if [ -f /usr/sbin/ethtool ]; then
        ETHTOOL=/usr/sbin/ethtool
    fi
fi

MIITOOL=""
if [ -f /sbin/mii-tool ]; then
    MIITOOL=/sbin/mii-tool
fi

echo 'begin details:'
for i in `ip -o link show 2>/dev/null | egrep '^[0-9]+:' | awk -F: '{print $2;}'`
do
    if [ -d /sys/class/net/$i ]; then
        echo begin /sys/class/net/$i:
        echo name: $i
        for file in address duplex ifindex speed
        do
            path=/sys/class/net/$i/$file
            if [ -r $path ]; then
                value=`cat $path 2>/dev/null`
                if [ "$value" != "" ]; then
                    echo $file: $value
                fi
            fi
        done

        if [ -d /sys/class/net/$i/bonding ]; then
            # Interface is a bonding master
            echo bonded: True
            for file in mode slaves
            do
                path=/sys/class/net/$i/bonding/$file
                if [ -r $path ]; then
                    value=`cat $path 2>/dev/null`
                    if [ "$value" != "" ]; then
                        echo bonding_$file: $value
                    fi
                fi
            done
        fi
        echo end /sys/class/net/$i:
    fi

    SUCCESS=1
    if [ "$ETHTOOL" != "" ]; then
        echo begin ethtool-$i:
        PRIV_ETHTOOL $ETHTOOL $i 2>/dev/null
        SUCCESS=$?
        echo end ethtool-$i:
    fi
    if [ "$MIITOOL" != "" -a $SUCCESS -ne 0 ]; then
        echo begin mii-tool-$i:
        PRIV_MIITOOL $MIITOOL -v $i 2>/dev/null
        echo end mii-tool-$i:
    fi
done
echo 'end details:'
fi

echo --- END ip_link_if

echo --- START ifconfig_if
ifconfig -a 2>/dev/null
if [ $? -eq 0 ]; then

ETHTOOL=""
if [ -f /sbin/ethtool ]; then
    ETHTOOL=/sbin/ethtool
else
    if [ -f /usr/sbin/ethtool ]; then
        ETHTOOL=/usr/sbin/ethtool
    fi
fi

MIITOOL=""
if [ -f /sbin/mii-tool ]; then
    MIITOOL=/sbin/mii-tool
fi

echo 'begin details:'
for i in `ifconfig -a 2>/dev/null | egrep '^[a-z]' | awk -F: '{print $1;}'`
do
    if [ -d /sys/class/net/$i ]; then
        echo begin /sys/class/net/$i:
        echo name: $i
        for file in address duplex ifindex speed
        do
            path=/sys/class/net/$i/$file
            if [ -r $path ]; then
                value=`cat $path 2>/dev/null`
                if [ "$value" != "" ]; then
                    echo $file: $value
                fi
            fi
        done

        if [ -d /sys/class/net/$i/bonding ]; then
            # Interface is a bonding master
            echo bonded: True
            for file in mode slaves
            do
                path=/sys/class/net/$i/bonding/$file
                if [ -r $path ]; then
                    value=`cat $path 2>/dev/null`
                    if [ "$value" != "" ]; then
                        echo bonding_$file: $value
                    fi
                fi
            done
        fi
        echo end /sys/class/net/$i:
    fi

    SUCCESS=1
    if [ "$ETHTOOL" != "" ]; then
        echo begin ethtool-$i:
        PRIV_ETHTOOL $ETHTOOL $i 2>/dev/null
        SUCCESS=$?
        echo end ethtool-$i:
    fi
    if [ "$MIITOOL" != "" -a $SUCCESS -ne 0 ]; then
        echo begin mii-tool-$i:
        PRIV_MIITOOL $MIITOOL -v $i 2>/dev/null
        echo end mii-tool-$i:
    fi
done
echo 'end details:'
fi

echo --- END ifconfig_if

# getNetworkConnectionList
echo --- START netstat

PRIV_NETSTAT netstat -aneep --tcp --udp -W 2>/dev/null
if [ $? -eq 4 ]; then
    # netstat failed due to invalid option, try -T
    PRIV_NETSTAT netstat -aneep --tcp --udp -T 2>/dev/null
    if [ $? -eq 4 ]; then
        # netstat still failed, try without any wide option
        PRIV_NETSTAT netstat -aneep --tcp --udp 2>/dev/null
    fi
fi

echo --- END netstat

echo --- START ss

PRIV_SS ss -aneptu 2>/dev/null

echo --- END ss

# getProcessList
echo --- START ps
ps -eo pid,ppid,uid,user,cmd --no-headers -ww 2>/dev/null

echo --- END ps

# getPatchList
#   ** DISABLED **

# getProcessToConnectionMapping
echo --- START lsof-i
PRIV_LSOF lsof -l -n -P -F ptPTn -i 2>/dev/null
echo --- END lsof-i

# getPackageList
echo --- START rpmx
rpm -qa --queryformat 'begin\nname: %{NAME}\nversion: %{VERSION}\nrelease: %{RELEASE}\narch: %{ARCH}\nvendor: %{VENDOR}\nepoch: %{EPOCH}\ndescription: %{SUMMARY}\nend\n' 2>/dev/null

echo --- END rpmx

echo --- START rpm
rpm -qa --queryformat %{NAME}:%{VERSION}:%{RELEASE}:%{ARCH}:%{EPOCH}@ 2>/dev/null

echo
echo --- END rpm

echo --- START dpkg
COLUMNS=256 dpkg -l '*' 2>/dev/null | egrep '^ii '

echo --- END dpkg

# getHBAList
echo --- START hba_sysfs
echo begin sysfs_hba:
if [ ! -d /sys/class ]; then
    echo /sys/class does not exist
elif [ -r /proc/vmware/version ] &&
     [ `grep -o -m1 ESX /proc/vmware/version` ] ||
     [ -r /etc/vmware-release ] &&
     [ `grep -o ESX /etc/vmware-release` ]; then
    echo ESX HBA drivers do not support sysfs
else
    for device in `ls /sys/class/fc_host 2>/dev/null`
    do
        systool -c fc_host -v $device
        systool -c scsi_host -v $device
    done
fi
echo end sysfs_hba:

echo --- END hba_sysfs

echo --- START hba_procfs
echo begin procfs_hba:
if [ ! -d /proc/scsi ]; then
    echo /proc/scsi does not exist
else
    for driver in 'qla*' 'lpfc*'
    do
        for device in `find /proc/scsi/$driver/* 2>/dev/null`
        do
            echo HBA Port $device
            cat $device
        done
    done
fi
echo end procfs_hba:

echo --- END hba_procfs

echo --- START hba_hbacmd

PATH=/usr/sbin/hbanyware:$PATH

echo begin hbacmd_listhbas:
PRIV_HBACMD hbacmd ListHBAs
echo end hbacmd_listhbas:

echo begin hbacmd_hbaattr:
for WWPN in `PRIV_HBACMD hbacmd ListHBAs 2>/dev/null | awk '/Port WWN/ {print $4;}'`
do
    PRIV_HBACMD hbacmd HBAAttrib $WWPN 2>/dev/null
done
echo end hbacmd_hbaattr:

echo --- END hba_hbacmd

# getServices
#   ** UNSUPPORTED **

# getFileSystems
echo --- START df
echo begin df:
PRIV_DF df -lk 2>/dev/null
echo end df:
echo begin mount:
mount 2>/dev/null
echo end mount:
echo begin xtab:
if [ -s /var/lib/nfs/xtab ]; then
    cat /var/lib/nfs/xtab
else
    if [ -s /var/lib/nfs/etab ]; then
        cat /var/lib/nfs/etab
    fi
fi
echo end xtab:
echo begin smbclient:
smbclient -N -L localhost
echo end smbclient:
echo begin smbconf:
configfile=`smbstatus -v 2>/dev/null | grep "using configfile" | awk '{print $4;}'`
if [ "$configfile" != "" ]; then
    if [ -r $configfile ]; then
        cat $configfile
    fi
fi
echo end smbconf:

echo --- END df

