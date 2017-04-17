#!/bin/sh

# Simple script to obtain host info from HPUX systems
# Script is divided into sections to match discovery methods

os=`uname -s`
if [ "$os" != "HP-UX" ]; then
    echo This script must be run on HPUX
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

stty kill '^U'
# - some HP-UX systems have the kill character set to @ - changed 
# it to something harmless


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

# lanadmin requires superuser privileges to display any interface speed
# and negotiation settings
PRIV_LANADMIN() {
    "$@"
}

# swlist requires superuser privileges to list all installed packages
PRIV_SWLIST() {
    "$@"
}

# fcmsutil requires superuser privileges to display any HBA information
PRIV_FCMSUTIL() {
    "$@"
}

# Formatting directive
echo FORMAT HPUX

# getDeviceInfo
echo --- START device_info

ihn=`hostname 2>/dev/null`
echo 'hostname:' $ihn
if [ -r /etc/resolv.conf ]; then
    echo 'dns_domain:' `awk '/^(domain|search)/ { print $2; exit }' /etc/resolv.conf 2>/dev/null`
fi
echo 'domain:' `domainname 2>/dev/null`
echo 'os:' `uname -sr 2>/dev/null`
echo 'series:' `uname -m 2>/dev/null | cut -d/ -f2 | cut -c1`

arch=""
case "`uname -m 2>/dev/null`" in
    ia64)
        arch="ia64" ;;
    9000/31?)
        arch="m68000" ;;
    9000/[34]??)
        arch="m68k" ;;
    9000/[678][0-9][0-9])
        case "`getconf SC_CPU_VERSION`" in
            523)
                arch="pa-risc 1.0" ;;
            528)
                arch="pa-risc 1.1" ;;
            532)
                if [ "`getconf SC_KERNEL_BITS`" = "64" ]; then
                    arch="pa-risc 2.0w"
                else
                    arch="pa-risc 2.0n"
                fi
                ;;
        esac ;;
esac
if [ "$arch" != "" ]; then
    echo "os_arch: $arch"
fi

echo --- END device_info

# getHostInfo
echo --- START host_info

OSVER=`uname -r 2>/dev/null | sed 's/[A-Z.]//g'`
kernel_bits=`getconf KERNEL_BITS 2>/dev/null`
if [ "$kernel_bits" = "" ]; then
    kernel_bits=32
fi
echo "kernel: $kernel_bits bit"
echo 'vendor: HP'
echo 'model:' `model 2>/dev/null`
hostid=`getconf CS_MACHINE_IDENT 2>/dev/null`
partition_id=`getconf CS_PARTITION_IDENT 2>/dev/null`
if [ "$partition_id" != "" ]; then
    echo 'partition_id:' $partition_id
    if [ "$hostid" = "" ]; then
        hostid="$partition_id"
    fi
    if [ "$partition_id" != "$hostid" ]; then
        hostid="$hostid:$partition_id"
    fi
fi
if [ "$hostid" != "" ]; then
    echo 'hostid:' $hostid
fi
serial=`getconf CS_MACHINE_SERIAL 2>/dev/null`
if [ "$serial" != "" ]; then
    echo 'serial:' $serial
fi
echo 'license:' `uname -l`

if [ -x /usr/contrib/bin/machinfo ]; then
    echo 'begin machinfo:'
    if [ $OSVER -ge 1131 ]; then
        /usr/contrib/bin/machinfo -v
    else
        /usr/contrib/bin/machinfo
    fi
    echo 'end machinfo:'
else
    logical_ram=""
    ram=""
    if [ -x /usr/sbin/parstatus ]; then
        par_id=`/usr/sbin/parstatus -w 2> /dev/null | grep "The local partition number is" | sed 's/.*The local partition number is \([0-9]*\)\..*/\1/'`
        if [ "$par_id" ]; then
            echo par_id: $par_id
            logical_ram=`/usr/sbin/parstatus -C -M |awk -v PAR_ID=$par_id 'BEGIN{ FS=":" };{ if ($NF==PAR_ID) { split($5, memarray,"/"); mem += int(memarray[1])} };END{ printf "logical_ram: %d\n", mem * 1048576 * 1024 }'`
        fi
    fi

    ram=`echo "selclass qualifier memory;infolog" | cstm 2>/dev/null | grep 'System Total'`
    if [ "$ram" != "" ]; then
        echo $ram | sed -e 's/^.*(\(.*\))[^0-9]*\([0-9]*\).*$/ram: \2\1/'
    fi

    if [ "$logical_ram" = "" ]; then
        # Get logical_ram from manifest for systems other than vpar
        if [ "$par_id" = "" ]; then
            # first try to check the Ignite-UX manifest
            MANIFEST_PATH="/var/opt/ignite/local/manifest/manifest"
            if [ -r $MANIFEST_PATH ]; then
                logical_ram=`grep "Main Memory" $MANIFEST_PATH | cut -d: -f2 | sed 's/ //g'`
                if [ "$logical_ram" != "" ]; then
                    echo "logical_ram: $logical_ram"
                fi
            fi
        fi
        if [ "$logical_ram" = "" ]; then  #cannot read manifest or cannot extract RAM info, try to read syslog
            SYSLOG_PATH="/var/adm/syslog/syslog.log"
            if [ -r $SYSLOG_PATH ]; then
                logical_ram=`grep "Physical" $SYSLOG_PATH | sed 's/^.*Physical: *\([0-9]*\).*\([GMK]\)bytes.*$/\1\2B/'`
                if [ "$logical_ram" != "" ]; then
                    echo "logical_ram: $logical_ram"
                fi
            fi
        fi   
    fi
fi

echo 'begin hpux_uptime_string:'
uptime 2>/dev/null
echo 'end hpux_uptime_string'

echo --- END host_info

# getMACAddresses
echo --- START lanscan_mac
lanscan | grep ETHER | awk '{print $2, $8;}'

echo --- END lanscan_mac

# getIPAddresses
echo --- START ifconfig_ip

NETSTAT_OUT=`netstat -inw 2>/dev/null`
if [ $? -ne 0 ]; then
    NETSTAT_OUT=`netstat -in 2>/dev/null`
fi
INTERFACES=`echo "$NETSTAT_OUT" | cut -d\  -f 1 | grep -Ev '\*|^Name|^lo0|^IP|^$' | sort -u`

for INTERFACE in $INTERFACES
do
  ifconfig $INTERFACE 2>/dev/null
done

echo --- END ifconfig_ip

# getNetworkInterfaces
echo --- START interface_commands

NETSTAT_OUT=`netstat -inw 2>/dev/null`
if [ $? -ne 0 ]; then
    NETSTAT_OUT=`netstat -in 2>/dev/null`
fi
INTERFACES=`echo "$NETSTAT_OUT" | cut -d\  -f 1 | grep -Ev '\*|^Name|^lo0|^IP|^$' | sort -u`

for INTERFACE in $INTERFACES
do
      ifconfig $INTERFACE 2>/dev/null
done
echo 'begin lanscan:'
lanscan -pia 2>/dev/null
echo 'end lanscan:'
echo 'begin lanadmin:'
for interface in `lanscan -p 2>/dev/null`
do
  echo begin-interface: $interface
  PRIV_LANADMIN lanadmin -x $interface 2>/dev/null | grep -v 'NO LINK'
  echo end-interface: $interface
done
echo 'end lanadmin:'
            

echo --- END interface_commands

# getNetworkConnectionList
echo --- START netstat
netstat -an -f inet 2>/dev/null
netstat -an -f inet6 2>/dev/null

echo --- END netstat

# getProcessList
echo --- START ps
if [ `uname -r | cut -d. -f2` -eq 11 ]; then
        if [ `uname -r | cut -d. -f3` -ge 11 ]; then
                PS_X_OPT=x
        fi
fi
env UNIX95=1 ps -e${PS_X_OPT}o pid,ppid,uid,user,args 2>/dev/null

echo --- END ps

# getPatchList
echo --- START patch_list

PRIV_SWLIST swlist -l product 2>/dev/null | egrep -v '^#' | egrep 'PH[A-Z]{2}_[0-9]+' | awk '{print $1, $2;}'

echo --- END patch_list

# getProcessToConnectionMapping
echo --- START lsof-i
PRIV_LSOF lsof -l -n -P -F ptPTn -i 2>/dev/null
echo --- END lsof-i

# getPackageList
echo --- START swlist

PRIV_SWLIST swlist -v -l product -a tag -a revision -a title -a vendor_tag 2>/dev/null

echo --- END swlist

# getHBAList
echo --- START fcmsutil

PATH=/opt/fcms/bin:$PATH

echo begin fcmsutil:
for device in `/usr/sbin/ioscan -kfnC fc 2>/dev/null | grep /dev`
do
    echo HBA attribute begin : $device
    PRIV_FCMSUTIL fcmsutil $device
    PRIV_FCMSUTIL fcmsutil $device vpd
    echo HBA attribute end : $device
done
echo end fcmsutil:
echo --- END fcmsutil

# getServices
#   ** UNSUPPORTED **

# getFileSystems
echo --- START bdf
echo begin df:
PRIV_DF bdf -l 2>/dev/null
echo end df:
echo begin mount:
mount -p 2>/dev/null
echo end mount:
echo begin xtab:
if [ -r /etc/xtab ]; then
    cat /etc/xtab
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

echo --- END bdf

