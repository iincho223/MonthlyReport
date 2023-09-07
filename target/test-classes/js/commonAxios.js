/** 
 * axiosを利用したpost通信
 * 正常終了：レスポンスデータ
 * 異常終了：null
 */
export async function postAxios(url, data) {
    const res = await axios.post(url, data, {headers: {'Content-Type': 'application/json'}})
        .catch(err => {return null});
    if (res.status != 200) {
        return null;
    }
    return res.data;
};
