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

