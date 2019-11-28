#!/bin/bash

set -e
pkcs12_file=""
keccak256sum=""
output_path="accounts_gm"
sm3_bin="/tmp/sm3"
sm3_tar="/tmp/sm3.tgz"
TASSL_CMD="${HOME}"/.tassl

help() {
    echo $1
    cat << EOF
Usage: $0 
    default       generate account and store private key in PEM format file
    -p            generate account and store private key in PKCS12 format file
    -k [FILE]     calculate address of PEM format [FILE]
    -P [FILE]     calculate address of PKCS12 format [FILE]
    -h Help
EOF

exit 0
}

prepare_keccak256()
{
    if [[ ! -f ${sm3_bin} ]];then
        if [ "$(uname)" == "Darwin" ];then
            // TODO: add support for macOS
            LOG_INFO "macOS is not supported yet."
            exit 0
            keccak256sum=""
            echo ${keccak256sum} | base64 -D - > ${sm3_tar}
        else
            keccak256sum="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"
            echo ${keccak256sum} | base64 -d - > ${sm3_tar}
        fi
        tar -zxf ${sm3_tar} -C /tmp && rm ${sm3_tar}
        chmod u+x ${sm3_bin}
    fi
    mkdir -p ${output_path}
}

# TASSL env
check_and_install_tassl()
{
    if [ ! -f "${HOME}/.tassl" ];then
        curl -LO https://github.com/FISCO-BCOS/LargeFiles/raw/master/tools/tassl.tar.gz
        LOG_INFO "Downloading tassl binary ..."
        tar zxvf tassl.tar.gz
        chmod u+x tassl
        mv tassl ${HOME}/.tassl
    fi
}

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

check_env() {
    check_and_install_tassl
}

calculate_address_pem()
{
    local pem_file=$1
    local no_print="$2"
    local suffix=${pem_file##*.}
    if [[ "${suffix}" != "pem" ]];then
        echo "The suffix of ${pem_file} is not pem. Please check it."
        exit 1
    fi
    prepare_keccak256
    privKey=$(${TASSL_CMD} ec -in ${pem_file} -text -noout 2>/dev/null| sed -n '3,5p' | tr -d ": \n" | awk '{print $0}')
    pubKey=$(${TASSL_CMD} ec -in ${pem_file} -text -noout 2>/dev/null| sed -n '7,11p' | tr -d ": \n" | awk '{print substr($0,3);}')
    echo ${pubKey}
    accountAddress=$(${sm3_bin}  ${pubKey})
    [ ! -z "${no_print}" ] || LOG_INFO "Account Address   : 0x${accountAddress}"
}

calculate_address_pkcs12()
{
    local p12_file=$1
    local pem_file="/tmp/.tmp.pem"
    local suffix=${p12_file##*.}
    if [[ "${suffix}" != "p12" && "${suffix}" != "pfx" ]];then
        echo "The suffix of ${p12_file} is neither p12 nor pfx. Please check it."
        exit 1
    fi
    ${TASSL_CMD} pkcs12 -in ${p12_file} -out ${pem_file} -nodes
    calculate_address_pem ${pem_file}
    rm ${pem_file}
}

generate_gmsm2_param()
{
    local output=$1
    cat << EOF > ${output} 
-----BEGIN EC PARAMETERS-----
BggqgRzPVQGCLQ==
-----END EC PARAMETERS-----
EOF
}

main()
{
    while getopts "k:pP:h" option;do
        case $option in
        k) calculate_address_pem "$OPTARG"
        exit 0;;
        P) calculate_address_pkcs12 "$OPTARG"
        exit 0;;
        p) #pkcs12_file="$OPTARG"
        pkcs12_file="true"
        ;;
        h) help;;
        esac
    done
    check_env
    prepare_keccak256
    if [ ! -f /tmp/gmsm2.param ];then
        generate_gmsm2_param /tmp/gmsm2.param
    fi
    ${TASSL_CMD} genpkey -paramfile /tmp/gmsm2.param -out ${output_path}/ecprivkey.pem
    calculate_address_pem ${output_path}/ecprivkey.pem "true"
    if [ -z "$pkcs12_file" ];then
        mv ${output_path}/ecprivkey.pem ${output_path}/0x${accountAddress}.pem
        LOG_INFO "Account Address   : 0x${accountAddress}"
        LOG_INFO "Private Key (pem) : ${output_path}/0x${accountAddress}.pem"
        # echo "0x${privKey}" > ${output_path}/${accountAddress}.private.hex
        ${TASSL_CMD} ec -in ${output_path}/0x${accountAddress}.pem -pubout -out ${output_path}/0x${accountAddress}.public.pem 2>/dev/null
        LOG_INFO "Public  Key (pem) : ${output_path}/0x${accountAddress}.public.pem"
    else
        ${TASSL_CMD} pkcs12 -export -name key -nocerts -inkey "${output_path}/ecprivkey.pem" -out "${output_path}/0x${accountAddress}.p12" || $(rm ${output_path}/0x${accountAddress}.p12 && rm ${output_path}/ecprivkey.pem && exit 1)
        ${TASSL_CMD} ec -in ${output_path}/ecprivkey.pem -pubout -out ${output_path}/0x${accountAddress}.public.p12 2>/dev/null
		rm ${output_path}/ecprivkey.pem
        LOG_INFO "Account Address   : 0x${accountAddress}"
        LOG_INFO "Private Key (p12) : ${output_path}/0x${accountAddress}.p12"
		LOG_INFO "Public  Key (p12) : ${output_path}/0x${accountAddress}.public.p12"
    fi
    # LOG_INFO "Private Key (hex) : 0x${privKey}"
    # echo "0x${pubKey}" > ${output_path}/${accountAddress}.public.hex
    # LOG_INFO "Public  File(hex) : ${output_path}/${accountAddress}.public.hex"
}

main $@

