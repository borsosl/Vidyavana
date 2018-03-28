const path = require('path');
const entry = require('webpack-glob-entry');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const now = new Date();
const tstamp = new Date(now.valueOf()-now.getTimezoneOffset()*60000).toISOString().replace(/\D/g, '').substr(0, 12);
const libtstamp = '20180307';

const commonDef = {
    resolve: {
        extensions: ['.ts']
    },
    module: {
        loaders: [
            {
                test: /\.ts$/,
                loader: 'ts-loader',
                options: {
                    onlyCompileBundledFiles: true
                }
            }
        ]
    },
    plugins: [
        new CleanWebpackPlugin('dist'),
        new UglifyJsPlugin({
            uglifyOptions: {
                ecma: 6,
                cache: true,
                compress: {
                    collapse_vars: true,
                    reduce_vars: false
                }
            }
        }),
        new CopyWebpackPlugin([{
            from: 'server/app/*.jsp',
            to: '../jsp/',
            flatten: true,
            transform(content) {
                let c = content.toString();
                c = c.replace(/<link rel="stylesheet" type="text\/css" href="\/css\/.*?\.css" \/>/g,
                    (found) => found.replace(/\/(.*?)\.css/, '/$1-'+tstamp+'.css'));
                c = c.replace('<script src="/js/lib.js"></script>', '<script src="/js/lib-'+libtstamp+'.js"></script>');
                c = c.replace(/<script src="\/js\/\D+?\.js"><\/script>/g,
                    (found) => found.replace(/\/(.*?)\.js/, '/$1-'+tstamp+'.js'));
                return c;
            }
        }, {
            from: 'server/ROOT/css',
            to: '../css/[name]-'+tstamp+'.[ext]',
            flatten: true,
            toType: 'template'
        }, {
            from: 'server/ROOT/js/lib.js',
            to: 'lib-'+libtstamp+'.js'
        }])
    ],
    externals: ['jquery'],
    devtool: "source-map"
};

const appDef = Object.assign({
    entry: entry('./server/js/app/*.ts'),
    output: {
        filename: '[name]-'+tstamp+'.js',
        path: path.resolve(__dirname, 'dist/js')
    }
}, commonDef);

module.exports = [appDef];
