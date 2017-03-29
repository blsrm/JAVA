#!/bin/bash

username=bmcadmin
patroluser=patrol
SUDO_FILE=/etc/sudoers

MKDIR_PATH=/usr/bin/mkdir
if [ ! -f "$MKDIR_PATH" ]; then
   MKDIR_PATH=/bin/mkdir
fi

CHOWN_PATH=/usr/bin/chown
if [ ! -f "$CHOWN_PATH" ]; then
   CHOWN_PATH=/bin/chown
fi

CHMOD_PATH=/usr/bin/chmod
if [ ! -f "$CHMOD_PATH" ]; then
   CHMOD_PATH=/bin/chmod
fi


existuserid=`grep "109" /etc/passwd | awk -F: '{print $3,$1}' | grep ^109 | awk '{print $1}'`
existusername=`grep "109" /etc/passwd | awk -F: '{print $3,$1}' | grep ^109 | awk '{print $2}'`
if [ "$existusername" != "$username" ];
then

grep "109" /etc/passwd | awk -F: '{print $3,$1}' | grep ^109
if [ $? = 0 ];
then
	/usr/sbin/useradd -s /bin/ksh -g 800 -c "Agentless Discovery" -m -d /home/$username $username
else
	/usr/sbin/useradd -s /bin/ksh -u 109 -g 800 -c "Agentless Discovery" -m -d /home/$username $username
fi

fi

chage -m 0 -M 99999 -I -1 -E -1 $username 
/usr/bin/passwd -d $username > /dev/null 2>&1
$MKDIR_PATH /home/$username/.ssh
$CHMOD_PATH 700 /home/$username/.ssh
echo -n "ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAmGPd5wuopDLwsQoYAO2BEEzILYPSB" > /home/$username/.ssh/authorized_keys
echo -n "r+sxLCI9w+1LfclZZdpr4xCGO5WRaMyBg4U8hHBWiuwr+SJ7qoBCenhwjgSIECPYjV" >> /home/$username/.ssh/authorized_keys
echo -n "X2qc1HY7F+FZmKcsXtsjiR3srG3sO68GiVko9RZCAWBWL6h0D0i6YBY405ochUV" >> /home/$username/.ssh/authorized_keys
echo "vgflD1dJAsyyM= CDNLinux@o2.com" >> /home/$username/.ssh/authorized_keys
echo -n "ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAy/yUktfe+4HCnqR5Qle34ER" >> /home/$username/.ssh/authorized_keys
echo -n "yCOhtplqyyhgnOrM1+ZCPv/vIdt45dWUk2lgD7kYCbGycBMEXaOL3z5" >> /home/$username/.ssh/authorized_keys
echo -n "0jtQunRGD96x9aI7N45rK4NnkHROaL4bRliffDvbDcauleDvIe7KjJxlK" >> /home/$username/.ssh/authorized_keys
echo "5li4TCRHJEvrnmmJFDllJYl0QsBnqG/Hm7CE= rsa-key-20131210" >> /home/$username/.ssh/authorized_keys
$CHMOD_PATH 600 /home/$username/.ssh/authorized_keys
$CHOWN_PATH $username:$patroluser /home/$username/.ssh/authorized_keys
$CHOWN_PATH $username:$patroluser /home/$username/.ssh
$CHOWN_PATH $username:$patroluser /home/$username



### SUDO file modification for bmcadmin
grep -i "User_Alias" $SUDO_FILE  | grep -i "BMC"

if [ $? = 1 ]; then
  echo "Adding BMC entry in Sudoers file - $SUDO_FILE"
  echo '' >> $SUDO_FILE
  echo 'User_Alias BMC=%patrol' >> $SUDO_FILE
  echo 'BMC ALL= ALL ' >> $SUDO_FILE
  echo 'bmcadmin ALL=(root) NOPASSWD: ALL' >> $SUDO_FILE
else
  grep -i "bmcadmin" $SUDO_FILE
  if [ $? = 1 ]; then
    echo "BMC profile Already Exist, Adding entry for bmcadmin in Sudoers file - $SUDO_FILE"
    echo '' >> $SUDO_FILE
    echo 'bmcadmin ALL=(root) NOPASSWD: ALL' >> $SUDO_FILE
  fi

fi


echo "Validation Steps ................................................."
echo
cat /etc/passwd | grep -i bmcadmin
cat /etc/group | grep -i ":800:"
ls -ld /home/bmcadmin
ls -ld /home/bmcadmin/.ssh
ls -l /home/bmcadmin/.ssh/authorized_keys
cat -n /home/bmcadmin/.ssh/authorized_keys
echo
echo
cat /etc/sudoers | grep -i bmc
echo
echo